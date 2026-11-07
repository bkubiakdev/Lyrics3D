# Lyrics3D

**Text from your favourite music in Minecraft.**

Play a song on your PC and its lyrics float around you in 3D, perfectly in sync, word by word. Classic, Neon or Scattered: pick a style and sing along.

Mod Fabric (tylko klient) dla Minecraft 1.21.1–1.21.11. Pokazuje w świecie 3D tekst piosenki odtwarzanej na komputerze (Spotify i inne odtwarzacze widoczne w sesjach multimediów Windows), zsynchronizowany z muzyką, z animacją słów. Wygląd, animacje, tryby (Cały wers, Słowo po słowie, Rozsypane), komety i edytor z podglądem na żywo odpowiadają modułowi Lyrics 3D z BKClient.

Teksty z czasami: lrc.red (czasy słów), LRCLIB, NetEase, Musixmatch. Mod jest darmowy, autor: bkubiakdev (https://bkubiak.dev/mods); przy pierwszym uruchomieniu pokazuje jednorazowo prośbę o wsparcie, a edytor ma link https://donate.bkubiak.dev.

## Użycie

- Prawy Shift (do zmiany w Sterowaniu) albo Mod Menu → Configure: edytor tekstów.
- Klawisz „Włącz/wyłącz teksty” (domyślnie bez przypisania).
- Komenda `/lyrics`: `on`, `off`, `toggle`, `editor`, `status`, `donate`, `preview on|off`, `preset classic|neon|scattered`, `offset [ms]`.
- Język moda: polski (gdy gra jest po polsku) albo angielski.
- Ustawienia: `config/lyrics3d.json`, stan edytora (format i paleta kolorów): `config/lyrics3d-ui.json`.
- Wymaga Fabric API. Mod Menu opcjonalnie.

## Budowanie

```
.\gradlew.bat buildAll
```

JAR-y: `v1.21.x/build/libs/Lyrics3D-1.0.0+mc1.21.x.jar`.

| JAR | Minecraft |
| --- | --- |
| mc1.21.1 | 1.21, 1.21.1 |
| mc1.21.3 | 1.21.2, 1.21.3 |
| mc1.21.4 | 1.21.4 |
| mc1.21.5 | 1.21.5 |
| mc1.21.6 | 1.21.6 |
| mc1.21.8 | 1.21.7, 1.21.8 |
| mc1.21.10 | 1.21.9, 1.21.10 |
| mc1.21.11 | 1.21.11 |

## Układ kodu

- `common/` – cały wspólny kod i zasoby (moduł, renderer napisów, pobieranie tekstów, ustawienia, edytor, fonty MSDF, shadery).
- `compat/` – warstwa renderu wybierana według wersji:
  - `shaderkey` (1.21.2–1.21.4) przekazuje do `RenderSystem`;
  - `legacy` (1.21.1) i `rawgl` (1.21.5+) rysują własnymi programami OpenGL przez `GlStateManager`;
  - `rawgl/GuiSurface` od 1.21.6 rysuje ekran edytora do własnej warstwy, którą GUI Minecrafta nakłada w całości;
  - `glyphs-*` obsługują czcionkę Minecraft (od 1.21.6 przechwycone glify).
- `v1.21.x/` – `Platform` (zdarzenia renderu świata, klawisze, link) i `CustomScreen` (wejście myszy i klawiatury w danej wersji).
- Brak mixinów. Render świata: 1.21.1–1.21.8 na końcu `WorldRenderer.render`, 1.21.9+ na końcu głównego przebiegu; tam główne wypełnienie liter zapisuje głębię, żeby chmury i pogoda pozostały za tekstem.

## Test w grze

`.\gradlew.bat :v1.21.11:runClient -Pworld=TestWorld -Psmoke=nazwa` przechodzi przez tryby Classic, Neon, Rozsypane, podążanie za kamerą i edytor. Zapisuje zrzuty w `v1.21.x/run/screenshots`. Klasy testowe (`dev/bkubiak/lyrics3d/dev`) nie trafiają do JAR-a.
