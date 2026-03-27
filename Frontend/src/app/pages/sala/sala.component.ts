(window as any).global = window;
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SalaService } from '../../core/services/sala.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { Sala } from '../../core/models/interfaces/sala.interface';
import { StampComponent } from '../../shared/components/stamp/stamp.component';
import { SlideInDirective } from '../../shared/directives/slide-in.directive';

@Component({
  selector: 'app-sala',
  standalone: true,
  imports: [FormsModule, RouterLink, StampComponent, SlideInDirective],
  templateUrl: './sala.component.html',
  styleUrls: ['./sala.component.scss']
})
export class SalaComponent implements OnInit {
  opcionActual: 'crear' | 'unirse' | 'retomar' = 'crear';
  nombreSala: string = '';
  codigoSala: string = '';
  fechaFormateada: string = '';
  fechaHeader: string = '';
  horaFormateada: string = '';
  nombreUsuario: string = '';

  private readonly mesesAbreviados = [
    'ENE','FEB','MAR','ABR','MAY','JUN',
    'JUL','AGO','SEP','OCT','NOV','DIC'
  ];

  constructor(
    private router: Router,
    private salaService: SalaService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.nombreUsuario = user?.usuario ?? 'DESCONOCIDO';

    const ahora = new Date();
    const dia = ahora.getDate().toString().padStart(2, '0');
    const mes = this.mesesAbreviados[ahora.getMonth()];
    const horas = ahora.getHours().toString().padStart(2, '0');
    const minutos = ahora.getMinutes().toString().padStart(2, '0');

    this.fechaFormateada = `${dia} ${mes} 1944`;
    this.fechaHeader = `${dia}/${mes}/44`;
    this.horaFormateada = `${horas}:${minutos} HRS`;
  }

  seleccionar(opcion: 'crear' | 'unirse' | 'retomar'): void {
    this.opcionActual = opcion;
  }

  get textoBoton(): string {
    const textos: Record<string, string> = {
      crear:   '· CREAR SALA ·',
      unirse:  '· INCORPORARSE ·',
      retomar: '· RETOMAR POSICIÓN ·'
    };
    return textos[this.opcionActual];
  }

  confirmar(): void {
    switch (this.opcionActual) {
      case 'crear':   this.onCrearSala();     break;
      case 'unirse':  this.onUnirseSala();    break;
      case 'retomar': this.onUnirsePartida(); break;
    }
  }

  generarNombreDefault(): string {
    const nombres = [
      'AGUILA', 'TORMENTA', 'CENTINELA',
      'HALCON', 'TITAN', 'FORTALEZA',
      'RELÁMPAGO', 'BASTIÓN', 'CÓNDOR'
    ];
    const num = Math.floor(Math.random() * 99) + 1;
    const nombre = nombres[Math.floor(Math.random() * nombres.length)];
    return `${nombre}-${num.toString().padStart(2, '0')}`;
  }

  private onCrearSala(): void {
    const nombre = this.nombreSala.trim() || `Operación ${this.generarNombreDefault()}`;
    this.salaService.crearSala({ nombreSala: nombre }).subscribe({
      next: (data: Sala) => {
        this.salaService.setSala(data);
        this.router.navigate(['/configPartida']);
      },
      error: (error) => {
        const msg = error.error?.mensaje ?? 'Ocurrió un error inesperado.';
        this.notificationService.error(msg);
        console.error('Error al crear sala:', error);
      }
    });
  }

  private onUnirseSala(): void {
    const url = this.codigoSala.trim();
    if (!url) {
      this.notificationService.error('Debe ingresar el código de operación.');
      return;
    }
    this.salaService.unirseSala(url).subscribe({
      next: (data: Sala) => {
        localStorage.setItem('urlSala', url);
        this.salaService.setSala(data);
        this.router.navigate(['/configPartida']);
      },
      error: (error) => {
        const msg = error.error?.mensaje ?? 'Ocurrió un error inesperado.';
        this.notificationService.error(msg);
        console.error('Error al unirse a sala:', error);
      }
    });
  }

  private onUnirsePartida(): void {
    const url = this.codigoSala.trim();
    const usuarioActual = this.authService.getCurrentUser();

    if (!usuarioActual?.idUsuario) {
      this.notificationService.error('No hay usuario logueado.');
      return;
    }
    if (!url) {
      this.notificationService.error('Debe ingresar el código de operación.');
      return;
    }

    this.salaService.unirsePartida(url, usuarioActual.idUsuario).subscribe({
      next: (data: any) => {
        const sala = data.configuracion;
        if (!sala) {
          this.notificationService.error('No se recibió configuración de sala válida.');
          return;
        }
        this.salaService.setSala(sala);
        this.router.navigate(['/juego', url]);
      },
      error: (error) => {
        const msg = error.error?.mensaje ?? error.error?.error ?? 'Ocurrió un error inesperado.';
        this.notificationService.error(msg);
        console.error('Error al retomar partida:', error);
      }
    });
  }
}
