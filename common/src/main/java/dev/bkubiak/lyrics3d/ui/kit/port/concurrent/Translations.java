package dev.bkubiak.lyrics3d.ui.kit.port.concurrent;
import java.util.*;
import dev.bkubiak.lyrics3d.ui.kit.port.enums.Language;
import dev.bkubiak.lyrics3d.ui.kit.port.util.TextHash;
public final class Translations {
 private static final Translations INSTANCE=new Translations();private final Map<String,String> words=new HashMap<>();private final List<java.lang.ref.WeakReference<Runnable>> listeners=new ArrayList<>();private Language language=Language.EN;
 private Translations(){try(var in=getClass().getResourceAsStream("/assets/lyrics3d/ui_translations.json")){var j=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();j.entrySet().forEach(e->words.put(e.getKey(),e.getValue().getAsString()));}catch(Exception e){throw new IllegalStateException("Missing UI translations",e);}}
 public static Translations getInstance(){return INSTANCE;} public Language getLanguage(){return language;}
 public void update(){language=Language.EN;listeners.removeIf(ref->ref.get()==null);listeners.forEach(ref->{var listener=ref.get();if(listener!=null)listener.run();});}
 public void update3(){}public void addRunnable(Runnable r){listeners.add(new java.lang.ref.WeakReference<>(r));}
 public String getStringByString(String hash){String text=words.get(hash);return text==null?null:dev.bkubiak.lyrics3d.systems.localization.Localizator.translate(text);}
 public String getStringByString2(String text){if(text==null)return null;return dev.bkubiak.lyrics3d.systems.localization.Localizator.translate(words.getOrDefault(Long.toHexString(TextHash.getLongByString(text)),text));}
}