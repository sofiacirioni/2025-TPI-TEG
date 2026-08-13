package ar.edu.utn.frc.tup.piii.Dtos.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshResponseDto {
    private String accessToken;
}
