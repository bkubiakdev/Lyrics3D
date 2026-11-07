# Lyrics3D

Text from your favourite music in Minecraft.

Lyrics3D shows the lyrics of the song you're listening to right in your Minecraft world. Put something on in Spotify (or any other music player on Windows), jump into the game, and the lyrics show up in front of you, in time with the music.

## What It Offers

* Lyrics floating in 3D in your world, synced to the song
* Works with Spotify and other music players on Windows
* Whole lines or word by word
* 3 styles: Classic, Neon and Scattered
* Live editor with a preview, open it with Right Shift or through Mod Menu
* Fonts, colors, gradients, glow, outline, size, distance and animations, all adjustable
* Lyrics can follow your camera or stay where they appeared
* Previous lines can stay on screen for a moment
* Sync offset if the lyrics come in a bit early or late
* `/lyrics` commands
* English and Polish (follows your game language, or pick one with `/lyrics language en|pl`)

## How It Works

Lyrics3D checks what's playing on your PC through the Windows media controls (the same thing that shows the song name when you change the volume). Then it looks up the song and downloads timed lyrics from lrc.red, LRCLIB, NetEase or Musixmatch.

The mod doesn't play any music and you don't need to log in to Spotify. It only reads what's already playing.

If a song doesn't have timed lyrics anywhere, nothing shows up. Most popular songs are covered, smaller artists not always.

## Commands

* `/lyrics` - status and list of commands
* `/lyrics on`, `/lyrics off`, `/lyrics toggle`
* `/lyrics editor` - opens the editor
* `/lyrics status` - shows the detected song and where the lyrics came from
* `/lyrics preset classic|neon|scattered`
* `/lyrics offset <ms>` - moves the lyrics earlier or later
* `/lyrics preview on|off` - test lines without music
* `/lyrics language en|pl|auto` - mod language (`auto` follows your game), `/lyrics en` and `/lyrics pl` work too
* `/lyrics donate`

## Requirements

* Fabric Loader
* Fabric API
* Minecraft 1.21.1 - 1.21.11 (download the file for your version)
* Windows, because the song is read from the Windows media controls
* Mod Menu is optional

## Notes

Lyrics3D is client-side only, so it works on any server and nobody else sees your lyrics.

The mod is free. If you like it, you can support it here: https://donate.bkubiak.dev

Made by bkubiakdev: https://bkubiak.dev/mods
