import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Sala} from '../models/interfaces/sala.interface';
import {catchError} from 'rxjs/operators';
import {throwError} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class accionesPanelService {
  constructor(private http: HttpClient) {
  }
}
