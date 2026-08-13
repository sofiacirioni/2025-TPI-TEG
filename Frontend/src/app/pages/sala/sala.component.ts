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
  opcionActual: 'crear' | 'unirse' = 'crear';
  nombreSala: string = '';
  codigoChars: string[] = ['', '', '', '', '', ''];
  fechaFormateada: string = '';
  fechaHeader: string = '';
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

    this.fechaFormateada = `${dia} ${mes} 194█`;
    this.fechaHeader = `${dia}/${mes}/4█`;
  }

  get codigoSala(): string {
    return this.codigoChars.join('');
  }

  seleccionar(opcion: 'crear' | 'unirse'): void {
    this.opcionActual = opcion;
    this.codigoChars = ['', '', '', '', '', ''];
  }

  get textoBoton(): string {
    const textos: Record<string, string> = {
      crear:  '· CREAR SALA ·',
      unirse: '· INCORPORARSE ·'
    };
    return textos[this.opcionActual];
  }

  confirmar(): void {
    switch (this.opcionActual) {
      case 'crear':  this.onCrearSala();  break;
      case 'unirse': this.onUnirseSala(); break;
    }
  }

  onCodigoInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    const val = input.value.replace(/[^A-Za-z0-9]/g, '').toUpperCase().slice(-1);
    this.codigoChars[index] = val;
    input.value = val;
    if (val && index < 5) {
      const boxes = document.querySelectorAll('.codigo-box');
      (boxes[index + 1] as HTMLInputElement)?.focus();
    }
  }

  onCodigoKeydown(index: number, event: KeyboardEvent): void {
    if (event.key === 'Backspace' && !this.codigoChars[index] && index > 0) {
      this.codigoChars[index - 1] = '';
      const boxes = document.querySelectorAll('.codigo-box');
      (boxes[index - 1] as HTMLInputElement)?.focus();
    }
  }

  onCodigoPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const text = (event.clipboardData?.getData('text') ?? '')
      .replace(/[^A-Za-z0-9]/g, '')
      .toUpperCase()
      .slice(0, 6);
    text.split('').forEach((c, i) => { this.codigoChars[i] = c; });
    // Focus last filled box
    const lastIndex = Math.min(text.length, 5);
    const boxes = document.querySelectorAll('.codigo-box');
    (boxes[lastIndex] as HTMLInputElement)?.focus();
  }

  generarNombreDefault(): string {
    const nombres = [
      'AGUILA', 'TORMENTA', 'CENTINELA',
      'HALCON', 'TITAN', 'FORTALEZA',
      'RELAMPAGO', 'BASTION', 'CONDOR'
    ];
    const num = Math.floor(Math.random() * 99) + 1;
    const nombre = nombres[Math.floor(Math.random() * nombres.length)];
    return `${nombre}-${num.toString().padStart(2, '0')}`;
  }

  private onCrearSala(): void {
    const nombre = this.nombreSala.trim() || `Operacion ${this.generarNombreDefault()}`;
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
    if (url.length < 6) {
      this.notificationService.error('Debe ingresar el código completo de 6 caracteres.');
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
}