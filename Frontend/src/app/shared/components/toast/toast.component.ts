import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { Notification, NotificationService, NotificationType } from '../../../core/services/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss']
})
export class ToastComponent implements OnInit, OnDestroy {

  notifications: (Notification & {
    visible: boolean;
    displayText: string;
  })[] = [];

  private sub!: Subscription;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.sub = this.notificationService.notifications$.subscribe(notifications => {
      // Agregar nuevas notificaciones con estado inicial
      notifications.forEach(n => {
        if (!this.notifications.find(e => e.id === n.id)) {
          const entry = { ...n, visible: false, displayText: '' };
          this.notifications.push(entry);
          // Trigger slide-in en el próximo frame
          setTimeout(() => {
            entry.visible = true;
            this.typewriter(entry);
          }, 50);
        }
      });
      // Marcar como no visibles las descartadas
      this.notifications.forEach(e => {
        if (!notifications.find(n => n.id === e.id)) {
          e.visible = false;
        }
      });
      // Limpiar las no visibles después de la transición
      setTimeout(() => {
        this.notifications = this.notifications.filter(
          e => notifications.find(n => n.id === e.id)
        );
      }, 500);
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  dismiss(id: string): void {
    this.notificationService.dismiss(id);
  }

  getLabel(type: NotificationType): string {
    const labels: Record<NotificationType, string> = {
      success: 'OK',
      error:   'ERROR',
      warning: 'ADVERTENCIA',
      info:    'INFO'
    };
    return labels[type];
  }

  /** Color de fondo del papel con tinte sutil según el tipo (6% blend sobre #EFE8CE) */
  getBackgroundColor(type: NotificationType): string {
    const colors: Record<NotificationType, string> = {
      success: '#E4E0C6', // #EFE8CE + 6% verde  (#375E41)
      error:   '#EADBC3', // #EFE8CE + 6% rojo   (#A01515)
      warning: '#E9DEC4', // #EFE8CE + 6% marrón (#884028)
      info:    '#E2DEC9', // #EFE8CE + 6% azul   (#1A4080)
    };
    return colors[type];
  }

  private typewriter(entry: { displayText: string; message: string }): void {
    let i = 0;
    const interval = setInterval(() => {
      if (i < entry.message.length) {
        entry.displayText += entry.message[i++];
      } else {
        clearInterval(interval);
      }
    }, 26);
  }
}
