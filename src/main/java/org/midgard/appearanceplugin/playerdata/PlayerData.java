package org.midgard.appearanceplugin.playerdata;

import org.midgard.appearanceplugin.utils.SkinUtils;

public class PlayerData {

	private String basicSkin;
	private String playerState;
	private String hat;
	private String chest;
	private String trousers;
	private String boots;

	public PlayerData(String basicSkin, String playerState, String hat, String chest, String trousers, String boots) {
		this.basicSkin = basicSkin;
		this.playerState = playerState;
		this.hat = hat;
		this.chest = chest;
		this.trousers = trousers;
		this.boots = boots;
	}

	public String getHat() {
		return hat;
	}

	public void setHat(String hat) {
		this.hat = hat;
	}

	public String getChest() {
		return chest;
	}

	public void setChest(String chest) {
		this.chest = chest;
	}

	public String getTrousers() {
		return trousers;
	}

	public void setTrousers(String trousers) {
		this.trousers = trousers;
	}

	public String getBoots() {
		return boots;
	}

	public void setBoots(String boots) {
		this.boots = boots;
	}

	public String getBasicSkin() {
		return basicSkin;
	}

	public void setBasicSkin(String basicSkin) {
		this.basicSkin = basicSkin;
	}

	public String getPlayerState() {
		return playerState;
	}

	public void setPlayerState(String playerState) {
		this.playerState = playerState;
	}

	public boolean isDifferentTo(PlayerData newData) {
		if (newData == null)
			return true;
		if (SkinUtils.getSkinFileName(this).equals(SkinUtils.getSkinFileName(newData)))
			return false;

		return true;
	}
}
