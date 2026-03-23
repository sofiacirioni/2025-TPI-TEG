/**
 * SlideInDirective — animación de entrada para elementos
 * tipo papel/documento sobre la mesa.
 *
 * Uso básico:
 *   <div appSlideIn>...</div>
 *
 * Con opciones:
 *   <div appSlideIn
 *        slideFrom="bottom"
 *        [slideDistance]="40"
 *        [delay]="150"
 *        [slideDuration]="600"
 *        slideEasing="cubic-bezier(0.2, 0.8, 0.3, 1.0)">
 *
 * Valores por defecto:
 *   slideFrom: 'bottom'
 *   slideDistance: 40px
 *   delay: 0ms
 *   slideDuration: 600ms
 *   slideEasing: 'cubic-bezier(0.2, 0.8, 0.3, 1.0)'
 *
 * IMPORTANTE: preserva transforms existentes en el elemento
 * (como rotate) — no los sobreescribe.
 *
 * Pantallas donde se usa actualmente:
 *   - InicioSesionComponent (pase-card, pase-actions)
 */
import { Directive, ElementRef, Input, OnInit } from '@angular/core';

@Directive({
  selector: '[appSlideIn]',
  standalone: true,
})
export class SlideInDirective implements OnInit {

  @Input() slideFrom: 'bottom' | 'top' | 'left' | 'right' = 'bottom';
  @Input() slideDistance: number = 40;
  @Input() delay: number = 0;
  @Input() slideDuration: number = 600;
  @Input() slideEasing: string = 'cubic-bezier(0.2, 0.8, 0.3, 1.0)';

  constructor(private elementRef: ElementRef) {}

  ngOnInit(): void {
    const el = this.elementRef.nativeElement as HTMLElement;

    el.style.opacity = '0';

    const fromTransform = this.getFromTransform();
    const existingTransform = getComputedStyle(el).transform;
    const baseTransform = existingTransform === 'none' ? '' : existingTransform;

    setTimeout(() => {
      const anim = el.animate(
        [
          {
            opacity: '0',
            transform: `${baseTransform} ${fromTransform}`.trim()
          },
          {
            opacity: '1',
            transform: baseTransform || 'none'
          }
        ],
        {
          duration: this.slideDuration,
          easing: this.slideEasing,
          fill: 'forwards'
        }
      );

      anim.onfinish = () => {
        el.style.opacity = '1';
      };
    }, this.delay);
  }

  private getFromTransform(): string {
    switch (this.slideFrom) {
      case 'bottom': return `translateY(${this.slideDistance}px)`;
      case 'top':    return `translateY(-${this.slideDistance}px)`;
      case 'left':   return `translateX(-${this.slideDistance}px)`;
      case 'right':  return `translateX(${this.slideDistance}px)`;
    }
  }
}