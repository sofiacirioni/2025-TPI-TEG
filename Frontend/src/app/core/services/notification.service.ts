import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
  id: string;
  type: NotificationType;
  message: string;
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  notifications$ = this.notificationsSubject.asObservable();

  success(message: string, duration = 4200): void {
    this.add('success', message, duration);
  }

  error(message: string, duration = 5000): void {
    this.add('error', message, duration);
  }

  warning(message: string, duration = 4200): void {
    this.add('warning', message, duration);
  }

  info(message: string, duration = 4200): void {
    this.add('info', message, duration);
  }

  dismiss(id: string): void {
    this.notificationsSubject.next(
      this.notificationsSubject.value.filter(n => n.id !== id)
    );
  }

  private add(type: NotificationType, message: string, duration: number): void {
    const id = crypto.randomUUID();
    const notification: Notification = { id, type, message, duration };
    this.notificationsSubject.next([
      ...this.notificationsSubject.value,
      notification
    ]);
    setTimeout(() => this.dismiss(id), duration);
  }
}
