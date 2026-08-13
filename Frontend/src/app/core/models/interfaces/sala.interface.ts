import {UsuarioDto} from '../../services/auth.service';

export interface Sala {
  idSala: number;
  nombreSala: string;
  url: string;
  creador:UsuarioDto;
}
export interface SalaGet{
  url: string;
}
