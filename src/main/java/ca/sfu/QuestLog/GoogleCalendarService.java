
package ca.sfu.QuestLog;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

// Epic 3: read-only Google Calendar access over plain REST calls.
//
// Needs three environment variables (see docs/EPIC3-planner.md):
//   GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, GOOGLE_REDIRECT_URI
// Without them the planner shows clearly-labelled sample events so it can still be demoed.
@Service
public class GoogleCalendarService {

    private static final String SCOPE = "https://www.googleapis.com/auth/calendar.readonly";
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final GoogleTokenRepository tokenRepository;
    private final RestClient http = RestClient.create();

    @Value("${GOOGLE_CLIENT_ID:}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET:}")
    private String clientSecret;

    @Value("${GOOGLE_REDIRECT_URI:}")
    private String redirectUri;

    public GoogleCalendarService(GoogleTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public boolean isConfigured() {
        return !clientId.isBlank() && !clientSecret.isBlank() && !redirectUri.isBlank();
    }

    public boolean isConnected(String email) {
        return tokenRepository.findByStudentEmail(email).isPresent();
    }

    public String authUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    // Called after Google redirects back with ?code=...
    public void handleCallback(String email, String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        Map<String, Object> res = tokenRequest(form);

        String accessToken = (String) res.get("access_token");
        String refreshToken = (String) res.get("refresh_token");
        Instant expiresAt = Instant.now().plusSeconds(((Number) res.get("expires_in")).longValue());

        GoogleToken token = tokenRepository.findByStudentEmail(email)
                .orElse(new GoogleToken(email, accessToken, refreshToken, expiresAt));
        token.update(accessToken, refreshToken, expiresAt);
        tokenRepository.save(token);
    }

    public void disconnect(String email) {
        tokenRepository.findByStudentEmail(email).ifPresent(tokenRepository::delete);
    }

    // Real events if the student connected Google, otherwise sample events.
    public List<CalendarItem> events(String email, Instant from, Instant to, ZoneId zone) {
        var token = tokenRepository.findByStudentEmail(email);
        if (token.isEmpty()) {
            return sampleEvents(from, zone);
        }
        return fetchGoogleEvents(token.get(), from, to, zone);
    }

    @SuppressWarnings("unchecked")
    private List<CalendarItem> fetchGoogleEvents(GoogleToken token, Instant from, Instant to, ZoneId zone) {
        String accessToken = freshAccessToken(token);

        URI uri = URI.create(UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/calendar/v3/calendars/primary/events")
                .queryParam("singleEvents", "true")
                .queryParam("orderBy", "startTime")
                .queryParam("maxResults", "100")
                .queryParam("timeMin", from.toString())
                .queryParam("timeMax", to.toString())
                .build()
                .encode()
                .toUriString());

        Map<String, Object> res = http.get()
                .uri(uri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(MAP_TYPE);

        List<CalendarItem> items = new ArrayList<>();
        if (res == null || !(res.get("items") instanceof List<?> raw)) {
            return items;
        }

        for (Object o : raw) {
            Map<String, Object> e = (Map<String, Object>) o;
            Map<String, Object> start = (Map<String, Object>) e.get("start");
            Map<String, Object> end = (Map<String, Object>) e.get("end");
            if (start == null || end == null) {
                continue;
            }
            boolean allDay = start.get("date") != null;
            String title = e.get("summary") != null ? (String) e.get("summary") : "(no title)";
            items.add(new CalendarItem(title,
                    parseGoogleTime(start, zone), parseGoogleTime(end, zone), allDay, "google"));
        }
        return items;
    }

    private Instant parseGoogleTime(Map<String, Object> t, ZoneId zone) {
        if (t.get("dateTime") != null) {
            return OffsetDateTime.parse((String) t.get("dateTime")).toInstant();
        }
        return LocalDate.parse((String) t.get("date")).atStartOfDay(zone).toInstant();
    }

    private String freshAccessToken(GoogleToken token) {
        if (token.getExpiresAt().minusSeconds(60).isAfter(Instant.now())) {
            return token.getAccessToken();
        }
        if (token.getRefreshToken() == null) {
            throw new IllegalStateException("Google session expired. Please reconnect.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("refresh_token", token.getRefreshToken());
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "refresh_token");

        Map<String, Object> res = tokenRequest(form);
        token.update((String) res.get("access_token"), null,
                Instant.now().plusSeconds(((Number) res.get("expires_in")).longValue()));
        tokenRepository.save(token);
        return token.getAccessToken();
    }

    private Map<String, Object> tokenRequest(MultiValueMap<String, String> form) {
        return http.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(MAP_TYPE);
    }

    // Demo events shown when Google is not connected.
    List<CalendarItem> sampleEvents(Instant now, ZoneId zone) {
        return List.of(
                sample("Lecture (sample)", now, zone, 1, 10, 2),
                sample("Club meeting (sample)", now, zone, 2, 17, 1),
                sample("Work shift (sample)", now, zone, 3, 13, 4),
                sample("Lab (sample)", now, zone, 5, 14, 2));
    }

    private CalendarItem sample(String title, Instant now, ZoneId zone,
                                int dayOffset, int hour, int lengthHours) {
        ZonedDateTime start = now.atZone(zone).toLocalDate().plusDays(dayOffset)
                .atTime(hour, 0).atZone(zone);
        return new CalendarItem(title, start.toInstant(),
                start.plusHours(lengthHours).toInstant(), false, "sample");
    }
}
