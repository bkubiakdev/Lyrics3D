package dev.bkubiak.lyrics3d.utility.sounds;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * lrc.red: free public lyrics API with word/syllable timing in TTML. Its terms ask for a User-Agent with contact
 * details (see {@link TimedLyricsSearch#CONTACT_AGENT}) and a visible credit, which is the source name in the
 * Lyrics 3D chat notice.
 */
public final class LrcRedLyricsProvider {
    public static final String HOST = "lrc.red";
    public static final String SOURCE = "lrc.red";
    private static final String BASE = "https://" + HOST;
    private static final String TTM = "http://www.w3.org/ns/ttml#metadata";
    private static final Pattern WORD = Pattern.compile("\\S+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern CLOCK = Pattern.compile("(?:(\\d+):)?(?:(\\d+):)?(\\d+(?:\\.\\d+)?)s?");
    /** A pause this long after a line clears it, like an empty LRC line does. */
    private static final long GAP_MS = 1500;
    private static final int MAX_DOWNLOADS = 2;

    /** {@code wordTimed}: at least one line carries real per-word times. */
    public record Found(TimedLyricsSearch.Result result, boolean wordTimed) { }
    private record Hit(String isrc, double seconds) { }

    private LrcRedLyricsProvider() { }

    public static Found search(String artist, String title, String rawTitle, long seconds, boolean approximate) {
        return search(artist, title, rawTitle, seconds, approximate, TimedLyricsSearch.NETWORK);
    }

    static Found search(String artist, String title, String rawTitle, long seconds, boolean approximate, TimedLyricsSearch.Transport network) {
        if (title == null || title.isBlank() || seconds <= 0) return new Found(empty("not_found"), false);
        title = TimedLyricsSearch.compactSpacedLetters(title);
        List<Hit> hits = new ArrayList<>();
        try {
            TimedLyricsSearch.Reply reply = network.request(BASE + "/search.json?q=" + enc((artist == null ? "" : artist + " ") + title), null);
            if (reply.code() == 429) return new Found(empty("rate_limited"), false);
            if (reply.code() != 200) return new Found(empty(reply.code() >= 500 ? "unavailable" : "not_found"), false);
            JsonObject json = JsonParser.parseString(reply.body()).getAsJsonObject();
            if (json.has("hits")) for (JsonElement e : json.getAsJsonArray("hits")) {
                try {
                    JsonObject h = e.getAsJsonObject();
                    if (artistMatches(h.get("artist").getAsString(), artist)
                        && TimedLyricsSearch.titleMatches(h.get("title").getAsString(), title, rawTitle == null ? "" : rawTitle))
                        hits.add(new Hit(h.get("isrc").getAsString(), h.get("duration").getAsDouble()));
                } catch (RuntimeException malformed) { /* Skip one entry, not the provider. */ }
            }
        } catch (Exception failure) { return new Found(empty("unavailable"), false); }
        hits.sort(Comparator.comparingDouble(h -> Math.abs(h.seconds - seconds)));
        int downloads = 0;
        for (Hit h : hits) {
            double delta = Math.abs(h.seconds - seconds);
            if (!Double.isFinite(h.seconds) || h.seconds <= 0 || delta > (approximate ? Math.min(15, seconds * .08) : 2)) continue;
            if (!h.isrc.matches("[A-Za-z0-9]{8,15}") || ++downloads > MAX_DOWNLOADS) continue;
            try {
                TimedLyricsSearch.Reply r = network.request(BASE + "/s/" + h.isrc + ".ttml", null);
                if (r.code() == 429) return new Found(empty("rate_limited"), false);
                if (r.code() != 200) continue;
                SyncedLyrics parsed = parseTtml(r.body());
                long actual = parsed.getLines().stream().filter(l -> !l.text().isBlank()).count();
                if (!parsed.isSynced() || actual < 2) continue;
                boolean words = parsed.getLines().stream().anyMatch(l -> l.wordMs() != null);
                return new Found(new TimedLyricsSearch.Result(parsed, SOURCE, delta > 2, Math.round(h.seconds), delta > 2 ? "approximate" : "ready"), words);
            } catch (Exception invalid) { /* Try the next recording. */ }
        }
        return new Found(empty("not_found"), false);
    }

    private static TimedLyricsSearch.Result empty(String status) {
        return new TimedLyricsSearch.Result(SyncedLyrics.EMPTY, "", false, 0, status);
    }

    /** lrc.red lists every performer in one field ("A, B"); the wanted artist must be one of them. */
    static boolean artistMatches(String candidate, String wanted) {
        String w = TimedLyricsSearch.norm(wanted);
        if (w.isEmpty()) return false;
        if (TimedLyricsSearch.norm(candidate).equals(w)) return true;
        for (String part : candidate.split("\\s*(?:,|&|\\bfeat\\.?|\\bft\\.?|\\bx\\b)\\s*"))
            if (TimedLyricsSearch.norm(part).equals(w)) return true;
        return false;
    }

    /** TTML → timed lines. Background vocals are skipped; a line keeps word times only if every word has one. */
    static SyncedLyrics parseTtml(String xml) {
        if (xml == null || xml.isBlank()) return SyncedLyrics.EMPTY;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            var builder = f.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler()); // malformed input: no stderr noise
            var doc = builder.parse(new InputSource(new StringReader(xml)));
            var ps = doc.getElementsByTagNameNS("*", "p");
            List<SyncedLyrics.LyricLine> lines = new ArrayList<>();
            long previousEnd = -1;
            for (int i = 0; i < ps.getLength(); i++) {
                Element p = (Element) ps.item(i);
                long begin = clock(p.getAttribute("begin")), end = clock(p.getAttribute("end"));
                if (begin < 0) continue;
                if (previousEnd >= 0 && begin - previousEnd >= GAP_MS) lines.add(new SyncedLyrics.LyricLine(previousEnd, ""));
                StringBuilder text = new StringBuilder();
                List<Long> times = new ArrayList<>();
                collect(p, -1, text, times);
                String raw = text.toString();
                int lead = 0;
                while (lead < raw.length() && raw.charAt(lead) == ' ') lead++;
                String line = SyncedLyrics.cleanText(raw);
                lines.add(new SyncedLyrics.LyricLine(begin, line, wordTimes(line, times.subList(Math.min(lead, times.size()), times.size()), begin)));
                previousEnd = end > begin ? end : -1;
            }
            if (previousEnd >= 0) lines.add(new SyncedLyrics.LyricLine(previousEnd, ""));
            if (lines.stream().noneMatch(l -> !l.text().isBlank())) return SyncedLyrics.EMPTY;
            lines.sort(Comparator.comparingLong(SyncedLyrics.LyricLine::timeMs));
            return new SyncedLyrics(lines, true);
        } catch (Exception malformed) {
            return SyncedLyrics.EMPTY;
        }
    }

    /** Appends the visible text and, per appended char, the begin time of the span it came from (-1 = untimed). */
    private static void collect(Node node, long time, StringBuilder text, List<Long> times) {
        for (Node c = node.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
                String s = c.getNodeValue();
                for (int i = 0; i < s.length(); i++) {
                    char ch = Character.isWhitespace(s.charAt(i)) ? ' ' : s.charAt(i);
                    if (ch == ' ' && (text.isEmpty() || text.charAt(text.length() - 1) == ' ')) continue;
                    text.append(ch);
                    times.add(ch == ' ' ? -1L : time);
                }
            } else if (c.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) c;
                String role = e.getAttributeNS(TTM, "role");
                if (role.startsWith("x-bg") || role.equals("x-translation") || role.equals("x-roman")) continue;
                long begin = clock(e.getAttribute("begin"));
                collect(e, begin >= 0 ? begin : time, text, times);
            }
        }
    }

    private static long[] wordTimes(String line, List<Long> times, long lineStart) {
        Matcher m = WORD.matcher(line);
        List<Long> words = new ArrayList<>();
        long last = lineStart;
        while (m.find()) {
            long t = -1;
            for (int i = m.start(); i < m.end() && i < times.size() && t < 0; i++) t = times.get(i);
            if (t < 0) return null;
            last = Math.max(last, t);
            words.add(last);
        }
        if (words.isEmpty()) return null;
        long[] out = new long[words.size()];
        for (int i = 0; i < out.length; i++) out[i] = words.get(i);
        return out;
    }

    /** TTML clock values: "12.345", "1:02.345", "1:02:03.4", optionally with an "s" suffix. -1 = missing. */
    static long clock(String value) {
        if (value == null || value.isBlank()) return -1;
        Matcher m = CLOCK.matcher(value.strip());
        if (!m.matches()) return -1;
        try {
            double seconds = Double.parseDouble(m.group(3));
            long a = m.group(1) == null ? 0 : Long.parseLong(m.group(1)), b = m.group(2) == null ? 0 : Long.parseLong(m.group(2));
            long hours = m.group(2) == null ? 0 : a, minutes = m.group(2) == null ? a : b;
            return Math.round(((hours * 60 + minutes) * 60 + seconds) * 1000);
        } catch (NumberFormatException e) { return -1; }
    }

    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}
