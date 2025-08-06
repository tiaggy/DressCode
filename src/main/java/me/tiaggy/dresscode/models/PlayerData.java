package me.tiaggy.dresscode.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class PlayerData {

    private String basicSkin;
    private String playerState;
    private String hat;
    private String chest;
    private String trousers;
    private String boots;
}
