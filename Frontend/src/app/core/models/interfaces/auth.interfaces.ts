export interface AuthResponse {
  accessToken: string;
  idUsuario:   number;
  usuario:     string;
  correo:      string;
  imagen:      string;
}

export interface RegisterRequest {
  usuario:     string;
  correo:      string;
  contrasenia: string;
  imagen:      string;
}

export interface RefreshResponse {
  accessToken: string;
}

export interface UserInfo {
  idUsuario: number;
  usuario:   string;
  correo:    string;
  imagen:    string;
}
