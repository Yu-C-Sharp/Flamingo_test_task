package api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("reason")
    private String reason;
}
