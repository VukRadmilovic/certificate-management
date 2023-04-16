import { Injectable } from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class InterceptorService implements HttpInterceptor {
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const accessToken: any = sessionStorage.getItem('user');
    const decodedItem = JSON.parse(accessToken);
    if (req.headers.get('skip')) {
      return next.handle(req);
    }

    if (accessToken && !req.url.includes('geocode')) {
      const cloned = req.clone({
        setHeaders: {
          Authorization: `Bearer ${decodedItem.accessToken}`
        }

      });
      return next.handle(cloned);
    } else {
      return next.handle(req);
    }
  }
}
