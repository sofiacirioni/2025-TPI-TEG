import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-intro',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './intro.component.html',
  styleUrls: ['./intro.component.scss']
})
export class IntroComponent implements OnInit, OnDestroy {
  lines: string[] = [];
  currentLine: string = '';
  showCursor: boolean = true;
  typingDone: boolean = false;
  isFadingOut: boolean = false;

  private cursorInterval: ReturnType<typeof setInterval> | null = null;
  private destroyed = false;

  constructor(private router: Router, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.startCursor();
    this.runSequence();
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    if (this.cursorInterval) {
      clearInterval(this.cursorInterval);
      this.cursorInterval = null;
    }
  }

  private startCursor(): void {
    this.cursorInterval = setInterval(() => {
      this.showCursor = !this.showCursor;
      this.cdr.markForCheck();
    }, 500);
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  private async typeText(text: string): Promise<void> {
    this.currentLine = '';
    for (const char of text) {
      if (this.destroyed) return;
      this.currentLine += char;
      this.cdr.detectChanges();
      await this.delay(45);
    }
  }

  private async runSequence(): Promise<void> {
    if (this.destroyed) return;

    const now = new Date();
    const months = ['ENE','FEB','MAR','ABR','MAY','JUN','JUL','AGO','SEP','OCT','NOV','DIC'];
    const day = String(now.getDate()).padStart(2, '0');
    const month = months[now.getMonth()];
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');

    await this.delay(300);

    await this.typeText('REGISTRO DE ACCESO \u2014 SALA DE OPERACIONES');
    this.lines.push(this.currentLine);
    this.currentLine = '';
    this.cdr.detectChanges();

    await this.delay(200);

    await this.typeText('NIVEL: ALTO SECRETO');
    this.lines.push(this.currentLine);
    this.currentLine = '';
    this.lines.push('\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500');
    this.cdr.detectChanges();

    await this.delay(400);

    await this.typeText(`FECHA........  ${day} ${month} 194\u2588 \u2014 ${hours}:${minutes}`);
    this.lines.push(this.currentLine);
    this.currentLine = '';
    this.cdr.detectChanges();

    await this.delay(200);

    await this.typeText('IDENTIFICACI\u00d3N...  \u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588');
    this.lines.push(this.currentLine);
    this.currentLine = '';
    this.lines.push('\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500');
    this.cdr.detectChanges();

    await this.delay(400);

    await this.typeText('ACCESO.....  AUTORIZADO');
    this.lines.push(this.currentLine);
    this.currentLine = '';
    this.typingDone = true;
    this.cdr.detectChanges();

    await this.delay(3000);

    if (this.cursorInterval) {
      clearInterval(this.cursorInterval);
      this.cursorInterval = null;
    }
    this.showCursor = false;
    this.cdr.detectChanges();

    this.isFadingOut = true;
    this.cdr.detectChanges();

    await this.delay(1000);

    if (!this.destroyed) {
      this.router.navigate(['/principal']);
    }
  }
}