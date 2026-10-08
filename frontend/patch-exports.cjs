const fs = require('fs');
const path = require('path');

function walk(dir) {
  let results = [];
  try {
    const list = fs.readdirSync(dir);
    list.forEach(file => {
      const fullPath = path.join(dir, file);
      const stat = fs.statSync(fullPath);
      if (stat && stat.isDirectory()) {
        if (file !== '.bin' && file !== '.git') {
          results = results.concat(walk(fullPath));
        }
      } else if (file === 'package.json') {
        results.push(fullPath);
      }
    });
  } catch(e) {}
  return results;
}

const nodeModules = path.join(__dirname, 'node_modules');
const pkgFiles = walk(nodeModules);

let count = 0;
pkgFiles.forEach(pkgPath => {
  try {
    const pkgDir = path.dirname(pkgPath);
    const content = fs.readFileSync(pkgPath, 'utf8');
    const json = JSON.parse(content);
    
    // Se o pacote tem pasta src/ e importam pacote/src/... ou pacote/private/...
    const srcDir = path.join(pkgDir, 'src');
    if (fs.existsSync(srcDir)) {
      const privateDir = path.join(pkgDir, 'private');
      if (!fs.existsSync(privateDir)) {
        try {
          fs.symlinkSync('src', privateDir, 'dir');
        } catch(e) {}
      }
    }
    
    if (json.exports) {
      delete json.exports;
      fs.writeFileSync(pkgPath, JSON.stringify(json, null, 2));
      count++;
    }
  } catch (err) {}
});

// Patch instantiateMetro.js: metro-core uses private #logLines — LogRespectingTerminal must use public log() instead
const instantiateMetroPath = path.join(nodeModules, '@expo/cli/build/src/start/server/metro/instantiateMetro.js');
if (fs.existsSync(instantiateMetroPath)) {
  let code = fs.readFileSync(instantiateMetroPath, 'utf8');
  if (code.includes('this._logLines.push(')) {
    code = code.replace(
      /\/\/ @ts-expect-error\s*\n\s*this\._logLines\.push\(\/\/ format args like console\.log\s*\n\s*_nodeUtil\(\)\.default\.format\(\.\.\.args\)\);\s*\n\s*\/\/ @ts-expect-error\s*\n\s*this\._scheduleUpdate\(\);/,
      '// metro-core now uses private #logLines — use the public log() method instead\n            this.log(_nodeUtil().default.format(...args));'
    );
    fs.writeFileSync(instantiateMetroPath, code);
  }
}

// Patch metro-transform-worker.js: several metro sub-modules have __esModule:true but no .default
const metroTransformWorkerPath = path.join(nodeModules, '@expo/metro-config/build/transform-worker/metro-transform-worker.js');
if (fs.existsSync(metroTransformWorkerPath)) {
  let code = fs.readFileSync(metroTransformWorkerPath, 'utf8');
  let changed = false;
  // metro-cache-key: only named export getCacheKey
  if (code.includes('__importDefault(require("metro-cache-key"))')) {
    code = code.replace(
      '__importDefault(require("metro-cache-key"))',
      '{ default: require("metro-cache-key").getCacheKey }'
    );
    changed = true;
  }
  // JsFileWrapping: __esModule:true but no .default — wrap the whole module as default
  if (code.includes('__importDefault(require("metro/src/ModuleGraph/worker/JsFileWrapping"))')) {
    code = code.replace(
      '__importDefault(require("metro/src/ModuleGraph/worker/JsFileWrapping"))',
      '{ default: require("metro/src/ModuleGraph/worker/JsFileWrapping") }'
    );
    changed = true;
  }
  if (changed) fs.writeFileSync(metroTransformWorkerPath, code);
}

// Patch serializer files: metro/src/DeltaBundler/Serializers/sourceMapString has __esModule:true but no .default
const serializerFiles = [
  path.join(nodeModules, '@expo/metro-config/build/serializer/serializeChunks.js'),
  path.join(nodeModules, '@expo/metro-config/build/serializer/withExpoSerializers.js'),
];
serializerFiles.forEach(filePath => {
  if (!fs.existsSync(filePath)) return;
  let code = fs.readFileSync(filePath, 'utf8');
  let changed = false;
  if (code.includes('__importDefault(require("metro/src/DeltaBundler/Serializers/sourceMapString"))')) {
    code = code.replace(
      /__importDefault\(require\("metro\/src\/DeltaBundler\/Serializers\/sourceMapString"\)\)/g,
      '{ default: require("metro/src/DeltaBundler/Serializers/sourceMapString").sourceMapString }'
    );
    changed = true;
  }
  if (changed) fs.writeFileSync(filePath, code);
});

// Patch runServer-fork.js: metro has __esModule:true but no .default export — _metro().default is undefined on Node 26
const runServerForkPath = path.join(nodeModules, '@expo/cli/build/src/start/server/metro/runServer-fork.js');
if (fs.existsSync(runServerForkPath)) {
  let code = fs.readFileSync(runServerForkPath, 'utf8');
  // Replace the lazy _metro() loader to fall back to the module itself when .default is absent
  // Only patch if not already patched
  if (!code.includes('data.default = raw;')) {
    code = code.replace(
      /function _metro\(\) \{[\s\S]*?_metro = function\(\) \{[\s\S]*?return data;\s*\};\s*return data;\s*\}/,
      `function _metro() {
    const raw = require("metro");
    const data = (raw && raw.__esModule && raw.default) ? raw : (raw && raw.__esModule) ? raw : { default: raw };
    if (!data.default || !data.default.createConnectMiddleware) {
      // metro exports named exports directly on the module object (no .default)
      data.default = raw;
    }
    _metro = function() { return data; };
    return data;
}`
    );
  }
  fs.writeFileSync(runServerForkPath, code);
}

// Patch @react-native/dev-middleware: missing "main" field breaks Node 26 CJS resolution
const devMiddlewarePkg = path.join(nodeModules, '@react-native/dev-middleware/package.json');
if (fs.existsSync(devMiddlewarePkg)) {
  const content = fs.readFileSync(devMiddlewarePkg, 'utf8');
  const json = JSON.parse(content);
  if (!json.main) {
    json.main = 'dist/index.js';
    fs.writeFileSync(devMiddlewarePkg, JSON.stringify(json, null, 2));
  }
}

console.log(`Compatibilidade Node 26 + Expo Metro concluída com sucesso!`);
