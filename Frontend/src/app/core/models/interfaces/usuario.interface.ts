import {UsuarioDto} from '../../services/auth.service';

export interface  Usuario{

  usuario : string;
  correo  : string;
  contrasenia:string;
  imagen : string;
}
