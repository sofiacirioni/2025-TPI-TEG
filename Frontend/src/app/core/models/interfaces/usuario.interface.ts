import {UsuarioDto} from '../../services/auth.service';

export interface  Usuario{

  nombre : string;
  apellido: string;
  correo  : string;
  contrasenia:string;
  imagen : string;
}
