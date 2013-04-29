/**
 * アプリケーションの起動時の状態を記録し、<br>
 * アプリケーション終了時にiniファイルを更新する必要があるかどうか<br>
 * 確認するためのクラス<br>
 */
public class AppState {
	private int windowX;
	private int windowY;
	private boolean flag;
	private String path;
	private String LFName;
	public AppState(int windowX, int windowY, boolean flag, String path, String LFName) {
		this.windowX = windowX;
		this.windowY = windowY;
		this.flag    = flag;
		this.path    = path;
		this.LFName  = LFName;
	}
	public int getWindowX() {
		return windowX;
	}
	public int getWindowY() {
		return windowY;
	}
	public boolean isFlag() {
		return flag;
	}
	public String getPath() {
		return path;
	}
	public String getLFName() {
		return LFName;
	}

	/**
	 * iniファイルを更新する必要がないなら true, あるなら false を返す。
	 * @return オブジェクトが等しいかどうか
	 */
	public boolean equals(AppState as) {
		if (windowX != as.getWindowX()) {
			return false;
		}
		if (windowY != as.getWindowY()) {
			return false;
		}
		if (flag != as.isFlag()) {
			return false;
		}
		if (!path.equals(as.getPath())) {
			return false;
		}
		if (!LFName.equals(as.getLFName())) {
			return false;
		}
		return true;
	}
}
