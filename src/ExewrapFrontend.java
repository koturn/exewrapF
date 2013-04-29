import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;


public class ExewrapFrontend extends JFrame {
	private static final String CHAR_CODE = "UTF-8";
	private static final String INIT_FILE_NAME = "exewrapF.ini";
	private static final int TEXT_FIELD_LENGTH = 30;
	private AppState appState;
	private JTextField jarTF;
	private JTextField exeTF;
	private JTextField iconTF;
	private JComboBox<String> appCB;
	private JComboBox<String> jreCB;
	private JTextField argTF;
	private JTextField versionTF;
	private JTextField copyrightTF;
	private JTextField descriptionTF;
	private JCheckBox[] checkBoxes;
	private String searchDirectory;
	private String exewrapFilePath;  // exewrapのファイルパス
	private boolean exewrapFlag;
	private JCheckBoxMenuItem[] LFMenuItems;
	private LookAndFeelInfo[] lf;  // 使用可能ルックアンドフィール

	private boolean isCopied;

	public static void main(String[] args) {
		final String[] fargs = args;
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ExewrapFrontend frame = new ExewrapFrontend("exewrapF");
				if (fargs.length > 0) {
					frame.readArguments(fargs);  // コマンドライン引数を読み込む
				}
				frame.setVisible(true);
			}
		});
	}


	public ExewrapFrontend(String title) {
		super(title);
		jarTF           = new JTextField(TEXT_FIELD_LENGTH);
		exeTF           = new JTextField(TEXT_FIELD_LENGTH);
		iconTF          = new JTextField(TEXT_FIELD_LENGTH);
		appCB           = new JComboBox<String>(new String[] {"コンソールアプリケーション", "ウィンドウアプリケーション", "サービスアプリケーション"});
		jreCB           = new JComboBox<String>(new String[] {"1.7", "1.6", "1.5", "1.4", "1.3", "1.2", "1.1", "1.0"});
		argTF           = new JTextField(TEXT_FIELD_LENGTH);
		versionTF       = new JTextField(TEXT_FIELD_LENGTH);
		copyrightTF     = new JTextField(TEXT_FIELD_LENGTH);
		descriptionTF   = new JTextField(TEXT_FIELD_LENGTH);
		checkBoxes      = new JCheckBox[] {new JCheckBox("SINGLE"), new JCheckBox("DDE_CONNECT"), new JCheckBox("NOLOG")};
		searchDirectory = ".";
		exewrapFilePath = "";
		exewrapFlag     = true;
		isCopied        = false;

		jreCB.setSelectedIndex(2);  // "1.5" をデフォルトで選択しておく

		initializeComponent();  // コンポーネントの配置を行う
		// ドラッグアンドドロップへの対応(テキストフィールドは個別に設定する必要がある。)
		new DropTarget(this,          DnDConstants.ACTION_COPY_OR_MOVE, new DragAndDropEventHandler());
		new DropTarget(jarTF,         DnDConstants.ACTION_COPY_OR_MOVE, new DragAndDropEventHandler());
		new DropTarget(exeTF,         DnDConstants.ACTION_COPY_OR_MOVE, new DragAndDropEventHandler());
		new DropTarget(iconTF,        DnDConstants.ACTION_COPY_OR_MOVE, new DragAndDropEventHandler());
		new DropTarget(argTF,         DnDConstants.ACTION_COPY_OR_MOVE, new DragAndDropEventHandler());
		new DropTarget(versionTF,     DnDConstants.ACTION_COPY_OR_MOVE, new DragAndDropEventHandler());
		new DropTarget(copyrightTF,   DnDConstants.ACTION_COPY_OR_MOVE, new DragAndDropEventHandler());
		new DropTarget(descriptionTF, DnDConstants.ACTION_COPY_OR_MOVE, new DragAndDropEventHandler());

		Runtime.getRuntime().addShutdownHook(new Thread() {  // シャットダウンフックの登録(匿名クラスを用いる)
			@Override
			public void run() {
				writeInitFile(INIT_FILE_NAME);
			}
		});

		readInitFile(INIT_FILE_NAME);  // .iniファイルを読み込む
		pack();  // ウィンドウを適切なサイズにする。
		setResizable(false);  // ウィンドウをリサイズ不可にする
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * コンポーネントの配置を行うメソッド
	 */
	public void initializeComponent() {
		InputStreamReader isr = null;
		Properties p = new Properties();
		try {
			isr = new InputStreamReader(getInputStreamInJar("resource/text/toolTipText.ini"), CHAR_CODE);
			p.load(isr);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch(UnsupportedEncodingException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		jarTF.setToolTipText(p.getProperty("jarTF", null));
		exeTF.setToolTipText(p.getProperty("exeTF", null));
		iconTF.setToolTipText(p.getProperty("iconTF", null));
		appCB.setToolTipText(p.getProperty("appCB", null));
		jreCB.setToolTipText(p.getProperty("jreCB", null));
		argTF.setToolTipText(p.getProperty("argTF", null));
		versionTF.setToolTipText(p.getProperty("versionTF", null));
		copyrightTF.setToolTipText(p.getProperty("copyrightTF", null));
		descriptionTF.setToolTipText(p.getProperty("descriptionTF", null));
		checkBoxes[0].setToolTipText(p.getProperty("SINGLE", null));
		checkBoxes[1].setToolTipText(p.getProperty("DDE_CONNECT", null));
		checkBoxes[2].setToolTipText(p.getProperty("NOLOG", null));

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu[] menus = {
				new JMenu("ファイル(F)"),
				new JMenu("設定(S)"),
				new JMenu("ヘルプ(H)")
		};
		menuBar.add(menus[0]);
		menuBar.add(menus[1]);
		menuBar.add(menus[2]);
		menus[0].setMnemonic(KeyEvent.VK_F);
		menus[1].setMnemonic(KeyEvent.VK_S);
		menus[2].setMnemonic(KeyEvent.VK_H);

		JMenuItem[] menuItems0 = {
				new JMenuItem("プロジェクトファイルを開く"),
				new JMenuItem("プロジェクトの保存"),
				new JMenuItem("EXEファイルの作成")
		};
		FileMenuEventHandler fmeh = new FileMenuEventHandler();
		menus[0].add(menuItems0[0]);
		menuItems0[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
		menuItems0[0].addActionListener(fmeh);

		menus[0].add(menuItems0[1]);
		menuItems0[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		menuItems0[1].addActionListener(fmeh);

		menus[0].addSeparator();

		menus[0].add(menuItems0[2]);
		menuItems0[2].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_DOWN_MASK));
		menuItems0[2].addActionListener(fmeh);

		JMenuItem menuItem = new JMenuItem("使用する exewrap の選択");
		menuItem.addActionListener(new ExewrapSettingEventHandler());
		menus[1].add(menuItem);

		menus[1].addSeparator();

		JMenu menu = new JMenu("Look&Feel");
		menus[1].add(menu);

		lf = UIManager.getInstalledLookAndFeels();  // 使用可能ルックアンドフィールの取得
		LFMenuItems = new JCheckBoxMenuItem[lf.length];
		LFChangeEventHandler lfceh = new LFChangeEventHandler();
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < lf.length; i++) {
			LFMenuItems[i] = new JCheckBoxMenuItem(lf[i].getName());
			LFMenuItems[i].addActionListener(lfceh);
			bg.add(LFMenuItems[i]);
			menu.add(LFMenuItems[i]);
		}
		LFMenuItems[0].setSelected(true);

		HelpMenuEventHandler heh = new HelpMenuEventHandler();
		JMenuItem[] menuItems2 = {
				new JMenuItem("ヘルプの表示"),
				new JMenuItem("バージョン情報"),
		};
		menus[2].add(menuItems2[0]);
		menuItems2[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		menuItems2[0].addActionListener(heh);

		menus[2].addSeparator();

		menus[2].add(menuItems2[1]);
		menuItems2[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
		menuItems2[1].addActionListener(heh);

		Container cont = getContentPane();

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		cont.add(toolBar, BorderLayout.NORTH);

		JButton bt = new JButton(getImageIconInJar("resource/image/open.png"));
		bt.setToolTipText(p.getProperty("openButton", null));
		bt.setActionCommand("プロジェクトファイルを開く");
		bt.addActionListener(fmeh);
		toolBar.add(bt);

		bt = new JButton(getImageIconInJar("resource/image/save.png"));
		bt.setToolTipText(p.getProperty("saveButton", null));
		bt.setActionCommand("プロジェクトの保存");
		bt.addActionListener(fmeh);
		toolBar.add(bt);

		Dimension d = new Dimension(6, 24);
		toolBar.addSeparator(d);

		ExecuteEventHandler eeh = new ExecuteEventHandler();
		bt = new JButton(getImageIconInJar("resource/image/make.png"));
		bt.setToolTipText(p.getProperty("makeButton", null));
		bt.setActionCommand("EXEファイルの作成");
		bt.addActionListener(eeh);
		toolBar.add(bt);

		toolBar.addSeparator(d);

		bt = new JButton(getImageIconInJar("resource/image/help.png"));
		bt.setToolTipText(p.getProperty("helpButton", null));
		bt.setActionCommand("ヘルプの表示");
		bt.addActionListener(heh);
		toolBar.add(bt);

		JLabel jarLabel         = new JLabel("対象JARファイル(.jar)");
		JLabel exeLabel         = new JLabel("出力EXEファイル(.exe)");
		JLabel iconLabel        = new JLabel("アイコンファイル(.ico)");
		JLabel appLabel         = new JLabel("アプリケーションの種類");
		JLabel jreLabel         = new JLabel("必要JREバージョン");
		JLabel argLabel         = new JLabel("JVM引数");
		JLabel versionLabel     = new JLabel("バージョン情報");
		JLabel copyrightLabel   = new JLabel("著作権");
		JLabel descriptionLabel = new JLabel("説明");

		JButton jarButton  = new JButton("参照");
		JButton exeButton  = new JButton("参照");
		JButton iconButton = new JButton("参照");

		jarButton.addActionListener(new FileReferenceEventHandler(jarTF, new FileNameExtensionFilter("JARファイル(*.jar)", "jar")));
		exeButton.addActionListener(new FileReferenceEventHandler(exeTF, new FileNameExtensionFilter("EXEファイル(*.exe)", "exe")));
		iconButton.addActionListener(new FileReferenceEventHandler(iconTF, new FileNameExtensionFilter("アイコンファイル(*.ico)", "ico")));


		JPanel panel = new JPanel();
		Component[][] components = {
				{jarLabel,          jarTF,          jarButton},
				{exeLabel,          exeTF,          exeButton},
				{iconLabel,         iconTF,         iconButton},
				{appLabel,          appCB,          iconButton},
				{jreLabel,          jreCB,          iconButton},
				{argLabel,          argTF,          iconButton},
				{versionLabel,      versionTF,      iconButton},
				{copyrightLabel,    copyrightTF,    iconButton},
				{descriptionLabel,  descriptionTF,  iconButton},
		};
		GroupLayout gl = makeGroupLayout(panel, components);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		panel.setLayout(gl);


		JPanel settingPanel = new JPanel();
		settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.Y_AXIS));
		settingPanel.add(panel);

		JPanel flagPanel = new JPanel();
		flagPanel.setLayout(new BoxLayout(flagPanel, BoxLayout.X_AXIS));
		flagPanel.setBorder(new TitledBorder("拡張フラグ"));
		flagPanel.add(checkBoxes[0]);
		flagPanel.add(checkBoxes[1]);
		flagPanel.add(checkBoxes[2]);

		settingPanel.add(flagPanel);
		cont.add(settingPanel, BorderLayout.CENTER);

		bt = new JButton("EXEファイルの作成");
		bt.setToolTipText(p.getProperty("makeButton", null));
		bt.addActionListener(eeh);
		JPanel makePanel = new JPanel();
		makePanel.add(bt);
		cont.add(makePanel, BorderLayout.SOUTH);

		setIconImage(getImageIconInJar("resource/icon/java-app.png").getImage());
		try {
			isr.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * GroupLayoutを作成する。<br>
	 * @param panel GroupLayoutを適用するパネル。<br>
	 * @param components コンポーネントの2次元配列。この配列の通りの配置になる。
	 * @return 作成したグループレイアウト
	 */
	private GroupLayout makeGroupLayout(JPanel panel, Component[][] components) {
		GroupLayout gl = new GroupLayout(panel);

		// 水平方向のグループ
		SequentialGroup hGroup = gl.createSequentialGroup();
		for (int i = 0; i < components[0].length; i++) {
			ParallelGroup pg = gl.createParallelGroup();
			for (int j = 0; j < components.length; j++) {
				pg = pg.addComponent(components[j][i]);
			}
			hGroup.addGroup(pg);
		}
		gl.setHorizontalGroup(hGroup);

		// 垂直方向のグループ
		SequentialGroup vGroup = gl.createSequentialGroup();
		for (int i = 0; i < components.length; i++) {
			ParallelGroup pg = gl.createParallelGroup(Alignment.BASELINE);
			for (int j = 0; j < components[0].length; j++) {
				pg = pg.addComponent(components[i][j]);
			}
			vGroup.addGroup(pg);
		}
		gl.setVerticalGroup(vGroup);
		return gl;
	}


	/**
	 * Jarファイル内の画像ファイルのImageIconアイコンを得る。
	 * Jarファイル内にファイルが存在しない場合、カレントディレクトリから、同一指定先を探す。
	 * それでも無ければ、返り値は空の ImageIcon である。
	 * @param Jarファイルにおける画像へのファイルパス
	 */
	public ImageIcon getImageIconInJar(String filePath) {
		URL url = getClass().getClassLoader().getResource(filePath);
		if (url != null) {
			return new ImageIcon(url);
		}
		return new ImageIcon(filePath);
	}

	/**
	 * Jarファイル内のリソースの InputStream を得る。
	 * Jarファイル内に指定リソースが存在しない場合、カレントディレクトリから同一指定先を探す。
	 *  それでも無ければ、返り値は null である。
	 * @param filePath Jarファイルにおけるリソースへのファイルパス
	 * @return リソースの InputStream
	 */
	public InputStream getInputStreamInJar(String filePath) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
		if (is != null) {
			return is;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filePath);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		return fis;
	}

	/**
	 * .iniファイルを読み込む
	 * @param filePath .iniファイルのパス
	 */
	private void readInitFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) return;
		Properties p = new Properties();
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), CHAR_CODE);
			p.load(isr);
			int x = Integer.parseInt(p.getProperty("windowX", ""));
			int y = Integer.parseInt(p.getProperty("windowY", ""));
			setLocation(x, y);

			exewrapFlag = Boolean.parseBoolean(p.getProperty("DefaultCheckBoxState", ""));
			exewrapFilePath = p.getProperty("exewrapFilePath", "");

			String LFName = p.getProperty("LookAndFeel", "");
			setLookAndFeel(LFName);
			for (JCheckBoxMenuItem c : LFMenuItems) {
				if (c.getText().equals(LFName)) {
					c.setSelected(true);
					break;
				}
			}
			appState = new AppState(x, y, exewrapFlag, exewrapFilePath, LFName);
			isr.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * .iniファイルを書き込む。
	 * @param filePath .iniファイルのパス(名前)
	 */
	private void writeInitFile(String filePath) {
		int x = getX();
		int y = getY();
		boolean flag = exewrapFlag;
		String path = exewrapFilePath;
		String lfName = null;
		for (JCheckBoxMenuItem c : LFMenuItems) {
			if (c.isSelected()) {
				lfName = c.getText();
				break;
			}
		}
		AppState endState = new AppState(x, y, flag, path, lfName);
		if (appState != null && endState.equals(appState)) return;

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), CHAR_CODE)));
			pw.println("windowX="              + x);
			pw.println("windowY="              + y);
			pw.println("DefaultCheckBoxState=" + flag);
			pw.println("exewrapFilePath="      + path.replace('\\', '/'));
			pw.println("LookAndFeel="          + lfName);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		} finally {
			pw.close();
		}
	}

	/**
	 * プロジェクトファイルの内容を反映させる。
	 * @param file プロジェクトファイル
	 */
	public void readProjectFile(File file) {
		Properties p = new Properties();
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), CHAR_CODE);
			p.load(isr);
			jarTF.setText(p.getProperty("jarFile", ""));
			exeTF.setText(p.getProperty("exeFile", ""));
			iconTF.setText(p.getProperty("iconFile", ""));
			argTF.setText(p.getProperty("argument", ""));
			versionTF.setText(p.getProperty("version", ""));
			copyrightTF.setText(p.getProperty("copyright", ""));
			descriptionTF.setText(p.getProperty("description", ""));

			appCB.setSelectedItem(p.getProperty("Application", "コンソールアプリケーション"));
			jreCB.setSelectedItem(p.getProperty("JRE-version", "1.5"));
			checkBoxes[0].setSelected(Boolean.parseBoolean(p.getProperty("SINGLE", "")));
			checkBoxes[1].setSelected(Boolean.parseBoolean(p.getProperty("DDE_CONNECT", "")));
			checkBoxes[2].setSelected(Boolean.parseBoolean(p.getProperty("NOLOG", "")));
			isr.close();
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(ExewrapFrontend.this, "プロジェクトファイルが見つかりませんでした", "警告", JOptionPane.WARNING_MESSAGE);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * プロジェクトファイルを保存する
	 * @param file 保存するプロジェクトファイル
	 */
	public void saveProjectFile(File file) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), CHAR_CODE)));
			pw.println("jarFile="     + jarTF.getText().replace('\\', '/'));
			pw.println("exeFile="     + exeTF.getText().replace('\\', '/'));
			pw.println("iconFile="    + exeTF.getText().replace('\\', '/'));
			pw.println("argument="    + argTF.getText());
			pw.println("version="     + versionTF.getText());
			pw.println("copyright="   + copyrightTF.getText());
			pw.println("description=" + descriptionTF.getText());
			pw.println("Application=" + (String)appCB.getSelectedItem());
			pw.println("JRE-version=" + (String)jreCB.getSelectedItem());
			pw.println("SINGLE="      + checkBoxes[0].isSelected());
			pw.println("DDE_CONNECT=" + checkBoxes[1].isSelected());
			pw.println("NOLOG="       + checkBoxes[2].isSelected());
			JOptionPane.showMessageDialog(ExewrapFrontend.this, "プロジェクトファイルを保存が完了しました。", "お知らせ", JOptionPane.INFORMATION_MESSAGE);
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(ExewrapFrontend.this, "プロジェクトファイルが見つかりませんでした", "警告", JOptionPane.WARNING_MESSAGE);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		} finally {
			pw.close();
		}
	}

	/**
	 * コマンドライン引数を読み込む
	 * @param args コマンドライン引数
	 */
	public void readArguments(String[] args) {
		for (String arg : args) {
			String str = arg.toLowerCase();
			if (str.endsWith(".jar")) {
				jarTF.setText(new File(str).getAbsolutePath());
			} else if (str.endsWith(".exe")) {
				exeTF.setText(new File(str).getAbsolutePath());
			} else if (str.endsWith(".ico")) {
				iconTF.setText(new File(str).getAbsolutePath());
			} else if (str.endsWith(".ewf")) {
				readProjectFile(new File(str));
				return;  // 読み込み終了(.ewfファイルが指定された時は、.ewfファイルの内容のみ反映する)
			}
		}
	}

	/**
	 * exeファイルを作成する。
	 */
	public void makeExe() {
		// System.out.println(getCommand());
		if (!new File(jarTF.getText()).exists()) {
			JOptionPane.showMessageDialog(ExewrapFrontend.this, "選択したJARファイルは存在しません", "注意", JOptionPane.WARNING_MESSAGE);
			return;
		}
		Process process = null;
		boolean isSuccess = false;
		try {
			process = Runtime.getRuntime().exec(getCommand());  // コマンドが実行できなければ、IOException を投げる
			process.waitFor();
			isSuccess = true;
		} catch (IOException ex) {  // コマンド実行が出来なかった場合
			if (isCopied) {
				JOptionPane.showMessageDialog(ExewrapFrontend.this, "exewrap.exe が見つかりません", "警告", JOptionPane.WARNING_MESSAGE);
				isCopied = false;
				return;
			}
			try {
				InputStream is = getClass().getClassLoader().getResourceAsStream("resource/lib/exewrap.exe");
				if (is != null) {
					OutputStream os = new FileOutputStream("exewrap.exe");
					copyStream(is, os, 8192);
					Thread.sleep(1000);
				}
				isCopied = true;
				makeExe();  // Jarファイル内部の exewrap をコピーした上で、もう一度実行
			} catch (FileNotFoundException ex2) {
				ex2.printStackTrace();
			} catch (IOException ex2) {
				ex2.printStackTrace();
			} catch (InterruptedException ex2) {
				ex2.printStackTrace();
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		isCopied = false;
		if (!isSuccess) return;  // コマンド実行に失敗していたら、return

		// 結果の出力
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			ArrayList<String> list = new ArrayList<String>();
			String str;
			while ((str = br.readLine()) != null) {
				list.add(str);
				// System.out.println(str);
			}
			Object[] obj = list.toArray();
			if (obj.length == 0) {
				obj = new String[] {"何らかのエラーが発生したため、", "EXE ファイルを作ることができませんでした。"};
			}
			JOptionPane.showMessageDialog(ExewrapFrontend.this, obj, "報告", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			process.destroy();  // プロセスの終了
			try {
				br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 入力ストリームから出力ストリームへデータの書き込みを行います。<br>
	 * 尚、コピー処理終了後、入力・出力ストリームを閉じます。
	 * @param in 入力ストリーム
	 * @param os 出力ストリーム
	 * @param bufferSize データの読み込みバッファサイズ(byte)です。
	 * @throws IOException 何らかの入出力処理例外が発生した場合
	 */
	public static void copyStream(InputStream in, OutputStream os, int bufferSize) throws IOException {
		int len = -1;
		byte[] b = new byte[bufferSize];
		try {
			while ((len = in.read(b, 0, b.length)) != -1) {
				os.write(b, 0, len);
			}
			os.flush();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * 実行するコマンドを生成する。
	 * @return 実行コマンド
	 */
	public String getCommand() {
		String str = null;
		if (!exewrapFlag && new File(exewrapFilePath).exists()) {
			str = "\"" + exewrapFilePath + "\" ";
		} else {  // 指定した exewrap が存在しないときも、カレントディレクトリの exewrap を使用
			str = "exewrap ";
		}
		StringBuilder sb = new StringBuilder(str);

		str = exeTF.getText();
		if (str.indexOf(':') != -1) {  // 絶対パス名が指定されているならば、
			sb.append("-o \"" + str + "\" ");
		} else {  // 無指定、もしくは相対パス名が指定されているならば、
			String jarFilePath = jarTF.getText();
			String path = new File(jarFilePath).getAbsolutePath();  // jarファイルの絶対パスを取得
			File dir = new File(path).getParentFile();  // jarファイルの親ディレクトリを取得

			String exeFileName = null;  // 出力 EXE ファイル名
			if (str.equals("")) {  // 入力がなければ、jarファイル名と同じ EXE ファイルを作る
				String jarFileName = new File(jarFilePath).getName();  // 変換jarファイル名
				exeFileName = jarFileName.substring(0, jarFileName.lastIndexOf('.')) + ".exe";
			} else {               // 入力があれば、入力されたファイル名にする。
				exeFileName = str;
			}

			sb.append("-o \"" + dir.getAbsolutePath() + "/" + exeFileName + "\" ");
		}

		str = iconTF.getText();
		if (!str.equals("")) {
			sb.append("-i \"" + str + "\" ");
		}

		str = (String)appCB.getSelectedItem();
		if (str.equals("ウィンドウアプリケーション")) {
			sb.append("-g ");
		} else if (str.equals("サービスアプリケーション")) {
			sb.append("-s ");
		}

		str = (String)jreCB.getSelectedItem();
		if (!str.equals("1.5")) {
			sb.append("-t " + str + " ");
		}

		str = argTF.getText();
		if (!str.equals("")) {
			sb.append("-a \"" + str + "\" ");
		}

		str = versionTF.getText();
		if (!str.equals("")) {
			sb.append("-v \"" + str + "\" ");
		}

		str = copyrightTF.getText();
		if (!str.equals("")) {
			sb.append("-c \"" + str + "\" ");
		}

		str = descriptionTF.getText();
		if (!str.equals("")) {
			sb.append("-d \"" + str + "\" ");
		}

		int cnt = 0;
		for (JCheckBox cb : checkBoxes) {
			if (cb.isSelected()) {
				cnt++;
			}
		}
		if (cnt > 0) {
			sb.append("-e ");
			for (JCheckBox cb : checkBoxes) {
				if (cb.isSelected()) {
					sb.append(cb.getText());
					if (--cnt > 0) {
						sb.append(";");
					}
				}
			}
			sb.append(" ");
		}
		sb.append("\"" + jarTF.getText() + "\"");
		return sb.toString();
	}

	/**
	 * ファイルを参照するイベントを行うクラス
	 */
	private class FileReferenceEventHandler implements ActionListener {
		private JTextField settingTF;
		private FileNameExtensionFilter filter;
		private FileReferenceEventHandler(JTextField settingTF, FileNameExtensionFilter filter) {
			this.settingTF = settingTF;
			this.filter    = filter;
		}
		@Override
		public void actionPerformed(ActionEvent ev) {
			JFileChooser fc = new JFileChooser(searchDirectory);  // デフォルトのディレクトリをカレントディレクトリに設定
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);  // ファイルのみを選択可能にする
			fc.setDialogTitle("開く");
			fc.addChoosableFileFilter(filter);
			int ret = fc.showOpenDialog(ExewrapFrontend.this);
			if (ret != JFileChooser.APPROVE_OPTION) return;  // 何もせずに編集画面に戻る
			searchDirectory = fc.getSelectedFile().getAbsolutePath();
			settingTF.setText(searchDirectory);
		}
	}

	/**
	 * "ファイル"メニューを選択したときの処理をするクラス
	 */
	private class FileMenuEventHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			String cmd = ev.getActionCommand();
			if (cmd.equals("プロジェクトファイルを開く")) {
				JFileChooser fc = new JFileChooser(searchDirectory);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);  // ファイルのみを選択可能にする
				fc.setDialogTitle("プロジェクトファイルを開く");
				fc.addChoosableFileFilter(new FileNameExtensionFilter("exewrapFプロジェクトファイル(*.ewf)", "ewf"));
				int ret = fc.showOpenDialog(ExewrapFrontend.this);

				if (ret != JFileChooser.APPROVE_OPTION) return;  // 何もせずに編集画面に戻る
				File file = fc.getSelectedFile();
				searchDirectory = file.getAbsolutePath();
				readProjectFile(file);
			} else if (cmd.equals("プロジェクトの保存")) {
				JFileChooser fc = new JFileChooser(searchDirectory);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);  // ファイルのみを選択可能にする
				fc.setDialogTitle("プロジェクトの保存");
				fc.setSelectedFile(new File("new.ewf"));
				fc.addChoosableFileFilter(new FileNameExtensionFilter("exewrapFプロジェクトファイル(*.ewf)", "ewf"));
				int ret = fc.showSaveDialog(ExewrapFrontend.this);

				if (ret != JFileChooser.APPROVE_OPTION) return;  // 何もせずに編集画面に戻る
				saveProjectFile(fc.getSelectedFile());
			} else if (cmd.equals("EXEファイルの作成")) {
				makeExe();
			}
		}
	}

	/**
	 * 使用する exewrap のファイルパスを指定するクラス
	 */
	private class ExewrapSettingEventHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			JPanel filePathPanel = new JPanel();
			filePathPanel.setLayout(new BoxLayout(filePathPanel, BoxLayout.X_AXIS));
			final JTextField tf = new JTextField(exewrapFilePath, 30);
			filePathPanel.add(tf, BorderLayout.CENTER);
			final JButton bt = new JButton("参照");
			filePathPanel.add(bt, BorderLayout.EAST);

			JRadioButton rb1 = new JRadioButton("カレントディレクトリのexewrapを使用する");
			JRadioButton rb2 = new JRadioButton("指定したexewrapを使用する");

			ButtonGroup bg = new ButtonGroup();
			bg.add(rb1);
			bg.add(rb2);

			if (exewrapFlag) {
				rb1.setSelected(true);
				tf.setOpaque(false);
				tf.setEditable(false);
				bt.setOpaque(false);
			} else {
				rb2.setSelected(true);
			}

			rb2.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent ev) {
					JRadioButton rb = (JRadioButton)ev.getSource();
					if (rb.isSelected()) {
						tf.setOpaque(true);
						tf.setEditable(true);
						bt.setOpaque(true);
						// bt.setVisible(true);
					} else {
						tf.setOpaque(false);
						tf.setEditable(false);
						bt.setOpaque(false);
						// bt.setVisible(false);
					}
				}
			});

			bt.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					JButton bt = (JButton)ev.getSource();
					if (!bt.isOpaque()) return;
					JFileChooser fc = null;
					if (new File(exewrapFilePath).exists()) {
						fc = new JFileChooser(exewrapFilePath);
					} else {
						fc = new JFileChooser(".");  // デフォルトのディレクトリをカレントディレクトリに設定
					}
					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);  // ファイルのみを選択可能にする
					fc.setDialogTitle("使用するexewrapの選択");
					fc.addChoosableFileFilter(new FileNameExtensionFilter("exewrap(*.exe)", "exe"));
					int ret = fc.showDialog(ExewrapFrontend.this, "選択");
					if (ret != JFileChooser.APPROVE_OPTION) return;  // 何もせずに編集画面に戻る
					tf.setText(fc.getSelectedFile().getAbsolutePath());
				}
			});

			int ret = JOptionPane.showConfirmDialog(ExewrapFrontend.this, new Object[] {rb1, rb2, filePathPanel}, "使用する exewrap の選択", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (ret != JOptionPane.YES_OPTION) return;
			exewrapFilePath = tf.getText();
			exewrapFlag = rb1.isSelected();
		}
	}

	/**
	 * ヘルプメニューのメニューアイテムのイベントを処理するクラス
	 */
	private class HelpMenuEventHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			String cmd = ev.getActionCommand();
			if (cmd.equals("ヘルプの表示")) {
				BufferedReader br;
				try {
					br = new BufferedReader(new InputStreamReader(getInputStreamInJar("resource/text/help.txt"), ExewrapFrontend.CHAR_CODE));
					StringBuilder sb = new StringBuilder();
					String str = null;
					while ((str = br.readLine()) != null) {
						sb.append(str);
						sb.append("\n");
					}
					br.close();
					new HelpWindow("ヘルプ", sb.toString()).setVisible(true);
				} catch (FileNotFoundException ex) {
					JOptionPane.showMessageDialog(ExewrapFrontend.this, "help.txt が見つかりません", "警告", JOptionPane.WARNING_MESSAGE);
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} else if (cmd.equals("バージョン情報")) {
				String[] msgs = {"exewrapF", "バージョン：1.9.2.2", "作者：koturn 0;"};
				JOptionPane.showMessageDialog(ExewrapFrontend.this, msgs, "バージョン情報", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		/**
		 * ヘルプ情報を表示するウィンドウ
		 */
		private class HelpWindow extends JFrame {
			public HelpWindow(String title, String text) {
				super(title);
				JEditorPane ep = new JEditorPane();
				ep.setEditable(false);
				ep.setContentType(ExewrapFrontend.CHAR_CODE);
				ep.setCursor(new Cursor(Cursor.TEXT_CURSOR));
				ep.setText(text);
				ep.setCaretPosition(0);
				// ep.setForeground(Color.WHITE);
				// ep.setBackground(Color.BLACK);

				JScrollPane sp = new JScrollPane(ep, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				getContentPane().add(sp);
				setIconImage(ExewrapFrontend.this.getIconImages().get(0));
				setBounds(ExewrapFrontend.this.getX() + 50, ExewrapFrontend.this.getY() + 50, 1000, 500);
				setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
		}
	}

	/**
	 * EXEファイルの生成を行うクラス
	 */
	public class ExecuteEventHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			makeExe();
		}
	}

	/**
	 * ドラッグアンドドロップを処理するクラス
	 */
	private class DragAndDropEventHandler implements DropTargetListener {
		@Override
		public void dragEnter(DropTargetDragEvent ev) {}

		@Override
		public void dragExit(DropTargetEvent ev) {}

		@Override
		public void dragOver(DropTargetDragEvent ev) {}

		@Override
		public void drop(DropTargetDropEvent ev) {
			ev.acceptDrop(DnDConstants.ACTION_MOVE);
			try {
				Transferable transfer = ev.getTransferable();
				if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					// java.util.List と java.awt.List の区別のため、Listではなく、java.util.List と書いた方がよい
					List<?> fileList = (List<?>)(transfer.getTransferData(DataFlavor.javaFileListFlavor));
					boolean flag = false;
					for (int i = 0; i < fileList.size(); i++) {
						File file = (File)fileList.get(i);
						String lowerCasefileName = file.getName().toLowerCase();
						if (lowerCasefileName.endsWith(".jar")) {
							jarTF.setText(file.getAbsolutePath());
						} else if (lowerCasefileName.endsWith(".exe")) {
							exeTF.setText(file.getAbsolutePath());
						} else if (lowerCasefileName.endsWith(".ico")) {
							iconTF.setText(file.getAbsolutePath());
						} else if (lowerCasefileName.endsWith(".ewf")) {
							readProjectFile(file);
							if (fileList.size() > 1) {
								String[] msgs = {"ドラッグアンドドロップしたファイルの中に", "プロジェクトファイルが含まれていたので、", "プロジェクトファイルを反映し、その他のファイルを無視しました。"};
								JOptionPane.showMessageDialog(ExewrapFrontend.this, msgs, "報告", JOptionPane.INFORMATION_MESSAGE);
								flag = false;
							}
							break;
						} else {
							flag = true;
						}
					}
					if (flag) {
						String[] msgs = {"関係のないファイル", "(拡張子が\".jar\", \".exe\", \".ico\", \".ewf\"以外のもの)", "は無視しました。"};
						JOptionPane.showMessageDialog(ExewrapFrontend.this, msgs, "報告", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				ev.dropComplete(false);
			}
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent ev) {}
	}

	/**
	 * ルックアンドフィールの切り替えを行うクラス
	 */
	private class LFChangeEventHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			setLookAndFeel(ev.getActionCommand());
		}
	}
	public void setLookAndFeel(String LFName) {
		String newLFClassName = "";
		for (LookAndFeelInfo info : lf) {
			if (LFName.equals(info.getName())) {
				newLFClassName = info.getClassName();
				break;
			}
		}
		try {
			UIManager.setLookAndFeel(newLFClassName);  // ルックアンドフィールの変更
			SwingUtilities.updateComponentTreeUI(ExewrapFrontend.this);  // ルックアンドフィールの更新通知
			ExewrapFrontend.this.pack();  //適切サイズに変更
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}
	}
}
