# Zander



## Luftraumdaten

Lufträume für Zander vorbereiten.

Die Luftraumdaten müssen zum Einlesen in einen binären Format vorliegen, welches mit den Zander-Programm (`WinZAN14`) erzeugt werden. 

Das Zander-Programm kann aber keine OpenAir-Daten bearbeiten, sondern nur Luftraumdaten in Zanderformat (text). Die aktuelle Lufträume sind leicht als OpenAir verfügbar aber das Zander-Programm kann diese nicht einwandfrei verarbeiten. Deshalb wurde das `ZanderFromOpenAir` Programm entwickelt, welches OpenAir files einliest und daraus Zander-Dateien (text) erzeugt.



### Ablauf

#### Luftraumdaten OpenAir (`.txt`)

Download:

* Deutschland: [DAeC](https://www.daec.de/fachbereiche/luftraum-flugbetrieb/luftraumdaten/) 
  Service - Download - Download Aktuelle Luftraumdaten
  * Luftraumdaten
* Segelflugsektoren Stuttgart: [BWLV](https://www.bwlv.de/verband-service/alle-downloads/arbeitskreis-sektoren.html)
  Verband/Service - Alle Downloads - Arbeitskreis Sektoren
  - Koordinaten aller Segelflugsektoren um EDDS
  - Wellenfluggebiet Schwarzwald



Dateien zu einer einzelne Datei vereinigen; aus den Segelflugsektoren-Daten nur die Segelflugsektoren übernehmen, doppelte Einträge vermeiden (offizielle Lufträume, z.B. `CTR`, `D`, ...).

Hinweis: die OpenAir Daten können mit den `KMLFromOpenAir` Programm in KML Dateien konvertiert werden und dann mit Google Earth überprüft werden. `KMLFromOpenAir.jar` kann mit Doppelklick gestartet werden, oder über die Eingabeaufforderung mit `java -jar KMLFromOpenAir.jar`.

#### Zander-Eingabe-Format (`.AZ`)

Die OpenAir Datei (`.txt`) mit Hilfe von `ZanderFromOpenAir` ([releases](https://github.com/fg-k/KML/releases)) in Zander-Format (`.AZ`) konvertieren. Die `ZanderFromOpenAir.jar` Datei kann mit Doppelklick gestartet werden, falls Java korrekt installiert wurde. Alternativ kann es über die Eingabeaufforderung gestartet werden: `java -jar ZanderFromOpenAir.jar` im Verzeichnis wo sich die `ZanderFromOpenAir.jar` Datei befindet.

#### Zander-Binär-Format (`.AZB`)

`WinSR02.exe` starten (direkt oder über `WinZAN.exe`). 

Unter `Bearbeiten` - `Luftraumdateien` - `prüfen` kann die `.AZ` Datei überprüft werden.

Mit `Datei` - `Luftraum` - `neue Luftraumdaten für SR940/ZS1 vorbereiten` wird die `.AZB` Datei aus der `.AZ` Datei erzeugt.

Computer über seriellen Kabel an den Rechner im Flieger verbinden und mit `Datei` - `Luftraum` - `neue Luftraumdaten in SR940/ZS1 schreiben` die `.AZB` Datei übertragen.



### Dateierweiterung

- OpenAir Luftraumdaten - `*.txt`
- Zander Eingabe-Format (Textdatei) - `*.AZ`
- Zander Binär-Datei - `*.AZB`



### Voraussetzung

- Java 8 (`JRE8`) - empfohlen wird die Version `8u202` von Oracle
- Das Zander-Programm (`WinZAN14`) 



## Links

- [Releases](https://github.com/fg-k/KML/releases) - `ZanderFromOpenAir` und `KMLFromOpenAir` Programme
- [ BWLV](https://www.bwlv.de/verband-service/alle-downloads/arbeitskreis-sektoren.html) - Baden-Württembergischer Luftfahrtverband e.V. - Arbeitskreis Sektoren
- [DAEC](https://www.daec.de/fachbereiche/luftraum-flugbetrieb/luftraumdaten/) - Deutscher Aero Club e.V. - Luftraumdaten
- [Java](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html) - Oracle - Java Archive Version 8
- [OpenJDK](https://jdk.java.net/java-se-ri/8-MR3) - OpenJDK - Java Reference Implementation
- [Zander](http://www.zander-variometer.de/) - Segelflugrechner

