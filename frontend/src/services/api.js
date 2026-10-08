import axios from 'axios';

// Instância Axios configurada com withCredentials=true para suportar cookies HttpOnly
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // Roteamento via API Gateway
  withCredentials: true, // Garante envio e recepção de cookies HttpOnly no navegador
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;
