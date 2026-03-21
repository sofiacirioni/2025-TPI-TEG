import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TransitionService {
  private _isTransitioning$ = new BehaviorSubject<boolean>(false);
  isTransitioning$: Observable<boolean> = this._isTransitioning$.asObservable();

  constructor(private router: Router) {}

  /**
   * Fade out current view and navigate.
   * Stub: navigates after `duration` ms. Full animation via global overlay TBD.
   */
  fadeTo(route: string, duration: number = 600): void {
    console.log(`[TransitionService] fadeTo ${route} (${duration}ms)`);
    this._isTransitioning$.next(true);
    setTimeout(() => {
      this.router.navigate([route]);
      this._isTransitioning$.next(false);
    }, duration);
  }

  /**
   * Zoom + crossfade to target background and navigate.
   * Stub: navigates after `duration` ms. Full animation via global overlay TBD.
   */
  zoomTo(route: string, targetBg: string, duration: number = 2400): void {
    console.log(`[TransitionService] zoomTo ${route} via ${targetBg} (${duration}ms)`);
    this._isTransitioning$.next(true);
    setTimeout(() => {
      this.router.navigate([route]);
      this._isTransitioning$.next(false);
    }, duration);
  }
}