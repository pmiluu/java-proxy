# java-proxy
http and https proxy in java


Działa dla obu protokołów 
Proxy uzywa portu 8080.

Abu uruchomić należy w ustawieniach proxy przeglądarki ustawić adres localhost, port 8080 i kliknac skrypt startowy o nazwie run.bat

1.Proxy czyta zapytanie z przegladarki
2.Dzieli je na na fragmenty czytając nazwe hosta
3.Wysyla odpowiednie nagłówki do serwera
4.Przesyla dane z serwera do przegladarki za pomoca metody sendData
