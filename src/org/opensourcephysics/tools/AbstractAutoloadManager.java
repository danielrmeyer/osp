/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;

/**
 * A dialog for managing autoloadable functions for a FunctionTool. The
 * functions are organized by search directory and fileName. Each function is described
 * by a name, expression and optional descriptor (eg track type for
 * TrackDataBuilder.AutoloadManager).
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public abstract class AbstractAutoloadManager extends JDialog {

	SearchPathDialog searchPathDialog;
	String searchPathChooserDir = OSPRuntime.getUserHome();
	Collection<String> searchPaths = new TreeSet<String>();
	JPanel functionPanel, instructionPanel;
	Box functionBox;
	Font lightFont, heavyFont;
	JButton closeButton, searchPathsButton;
	JTextArea instructionArea;
	Dimension defaultSize = new Dimension(450, 400);
	Map<String, Map<String, ArrayList<String[]>>> autoloadData;
	int inset0 = 6, inset1 = 20, inset2 = 40;
	boolean refreshing = false;
	protected boolean initialized = false;

	/**
	 * Constructor for a dialog, typically a FunctionTool.
	 * 
	 * @param dialog the dialog
	 */
	protected AbstractAutoloadManager(JDialog dialog) {
		super(dialog, true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		createGUI();
		Dimension dim = new Dimension(defaultSize);
		double factor = 1 + FontSizer.getLevel() * 0.25;
		dim.width = (int) (dim.width * factor);
		dim.height = (int) (dim.height * factor);
		setSize(dim);
	}

	/**
	 * Sets the autoload data. The data is a map of directory path to directory
	 * contents, where directory contents is a map of file path to file contents,
	 * file contents is a list of function arrays, and each function array is
	 * {String name, String expression, optional String descriptor)
	 * 
	 * @param data the data
	 */
	public void setAutoloadData(Map<String, Map<String, ArrayList<String[]>>> data) {
		autoloadData = data;
		refreshFunctionList();
	}

	/**
	 * Sets the instructions describing how to use this manager.
	 * 
	 * @param instructions the instructions
	 */
	public void setInstructions(String instructions) {
		instructionArea.setText(instructions);
		if (instructions != null) {
			getContentPane().add(instructionPanel, BorderLayout.NORTH);
		} else {
			getContentPane().remove(instructionPanel);
		}
	}

	/**
	 * Add a search path.
	 * 
	 * @param dir the (directory) search path to add
	 */
	public void addSearchPath(String dir) {
		searchPaths.add(XML.forwardSlash(dir));
	}

	/**
	 * Gets the collection (shallow clone) of search paths.
	 * 
	 * @return the search paths
	 */
	public Collection<String> getSearchPaths() {
		Collection<String> paths = new TreeSet<String>();
		paths.addAll(searchPaths);
		return paths;
	}

	/**
	 * Creates the GUI.
	 */
	protected void createGUI() {
		heavyFont = new JLabel().getFont().deriveFont(Font.BOLD);
		lightFont = heavyFont.deriveFont(Font.PLAIN);

		// create instructions
		instructionArea = new JTextArea();
		instructionArea.setEditable(false);
		instructionArea.setLineWrap(true);
		instructionArea.setWrapStyleWord(true);
		Border etched = BorderFactory.createEtchedBorder();
		Border empty = BorderFactory.createEmptyBorder(2, 4, 2, 4);
		instructionArea.setBorder(BorderFactory.createCompoundBorder(etched, empty));
		instructionArea.setForeground(Color.blue);
		instructionPanel = new JPanel(new BorderLayout());
		instructionPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		instructionPanel.add(instructionArea, BorderLayout.CENTER);

		// create function panel
		functionPanel = new JPanel(new BorderLayout());
		functionBox = Box.createVerticalBox();
		functionBox.setBackground(Color.white);
		functionBox.setOpaque(true);
		refreshFunctionList();

		JScrollPane scroller = new JScrollPane(functionBox);
		scroller.getVerticalScrollBar().setUnitIncrement(8);
		functionPanel.add(scroller, BorderLayout.CENTER);
		functionPanel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

		// create buttons and buttonbar
		closeButton = new JButton();
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		searchPathsButton = new JButton();
		searchPathsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// show search dialog
				if (searchPathDialog == null) {
					searchPathDialog = new SearchPathDialog();
					searchPathDialog.setLocationRelativeTo(AbstractAutoloadManager.this);
				}
				FontSizer.setFonts(searchPathDialog, FontSizer.getLevel());
				searchPathDialog.refreshGUI();
				searchPathDialog.refreshFileList();
				searchPathDialog.setVisible(true);
			}
		});
		JPanel buttonbar = new JPanel();
		buttonbar.add(searchPathsButton);
		buttonbar.add(closeButton);

		// assemble content pane
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		contentPane.add(functionPanel, BorderLayout.CENTER);
		contentPane.add(buttonbar, BorderLayout.SOUTH);

		refreshGUI();
	}

	/**
	 * Refreshes the GUI including locale-based resource strings.
	 */
	protected void refreshGUI() {
		setTitle(ToolsRes.getString("AutoloadManager.Title")); //$NON-NLS-1$
		closeButton.setText(ToolsRes.getString("Button.OK")); //$NON-NLS-1$
		searchPathsButton.setText(ToolsRes.getString("AutoloadManager.Button.SearchPaths") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		refreshFunctionList();
	}

	/**
	 * Refreshes the function list.
	 */
	protected void refreshFunctionList() {
		// refresh list of functions
		refreshing = true;
		functionBox.removeAll();
		if (autoloadData == null)
			return;
		String directoryTitle = ToolsRes.getString("AutoloadManager.Directory") + ": "; //$NON-NLS-1$ //$NON-NLS-2$
		for (String dir : autoloadData.keySet()) {
			Box dirBox = Box.createVerticalBox();
			TitledBorder border = BorderFactory.createTitledBorder(directoryTitle + XML.forwardSlash(dir));
			Font titleFont = FontSizer.getResizedFont(heavyFont, FontSizer.getLevel());
			border.setTitleFont(titleFont);
			Border spacer = BorderFactory.createEmptyBorder(6, 0, 6, 0);
			dirBox.setBorder(BorderFactory.createCompoundBorder(spacer, border));
			functionBox.add(dirBox);
			Map<String, ArrayList<String[]>> functionMap = autoloadData.get(dir);
			

			// display files in alphabetical order ignoring case
			Map<String, String> lowercaseNames = new TreeMap<String, String>();
			for (String fileName : functionMap.keySet()) {
				lowercaseNames.put(fileName.toLowerCase(), fileName);
			}
			for (String lowercase : lowercaseNames.keySet()) {
				int top = dirBox.getComponentCount() == 0 ? 0 : 10;
				String fileName = lowercaseNames.get(lowercase);
				File file = new File(dir, fileName);
				String filePath = XML.forwardSlash(file.getAbsolutePath());

				AutoloadFileCheckbox fileCheckbox = new AutoloadFileCheckbox(dir, fileName);
				fileCheckbox.setBorder(BorderFactory.createEmptyBorder(top, inset1, 2, 0));
				fileCheckbox.setFont(heavyFont);
				fileCheckbox.setSelected(isFileSelected(filePath));

				Box bar = Box.createHorizontalBox();
				bar.add(fileCheckbox);
				bar.add(Box.createHorizontalGlue());
				dirBox.add(bar);

				Border empty = BorderFactory.createEmptyBorder(0, 0, 0, 40);
				ArrayList<String[]> functionList = functionMap.get(fileName);
				if (functionList.isEmpty()) {
					Box labelBox = getEmptyMessage(inset2);
					dirBox.add(labelBox);
				}

				for (String[] f : functionList) {
					// add category (track type) if present
					JLabel label = null;
					if (f.length > 3) {
						String s = "[" + f[3] + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						label = new JLabel(s);
						label.setBorder(empty);
						label.setFont(lightFont);
					}
					AutoloadFunctionCheckbox checkbox = new AutoloadFunctionCheckbox(fileCheckbox, f);
					checkbox.setFont(lightFont);
					checkbox.setSelected(isFunctionSelected(filePath, f));

					bar = Box.createHorizontalBox();
					bar.add(checkbox);
					bar.add(Box.createHorizontalGlue());
					if (label != null)
						bar.add(label);
					bar.setBorder(BorderFactory.createEmptyBorder(0, inset2, 0, 0));

					dirBox.add(bar);
				}

//				fileCheckbox.reportSelectionStatus();

			}
			if (dirBox.getComponentCount() == 0) {
				dirBox.add(getEmptyMessage(inset1));
			}
		}
		if (functionBox.getComponentCount() == 0) {
			functionBox.add(getEmptyMessage(inset0));
		}
		FontSizer.setFonts(functionBox, FontSizer.getLevel());
		refreshing = false;
	}

	/**
	 * Sets the font level.
	 *
	 * @param level the desired font level
	 */
	public void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		FontSizer.setFonts(instructionArea, level);
	}
	
	public String[][] getAllFunctions(String filePath) {
		String dir = XML.getDirectoryPath(filePath);
		Map<String, ArrayList<String[]>> functionMap = autoloadData.get(dir);
		if (functionMap == null) 
			return null;
		ArrayList<String[]> functionList = functionMap.get(XML.getName(filePath));
		if (functionList == null) 
			return null;
		return functionList.toArray(new String[functionList.size()][]);
	}

	/**
	 * Sets the selection state of a function.
	 *
	 * @param filePath the path to the file defining the function
	 * @param function the function {name, expression, optional descriptor}
	 * @param select   true to select the function
	 */
	protected void setFunctionSelected(String filePath, String[] function, boolean select) {
		String[] oldExclusions = getExclusionsMap().get(filePath);
		String[] newExclusions = null;
		if (!select) {
			// create or add entry to newExclusions
			if (oldExclusions == null) {
				newExclusions = new String[] { function[0] };
			} else {
				int n = oldExclusions.length;
				if (getAllFunctions(filePath).length == n + 1) {
					newExclusions = new String[] { "*" };
				}
				else {
					newExclusions = new String[n + 1];
					System.arraycopy(oldExclusions, 0, newExclusions, 0, n);
					newExclusions[n] = function[0];
				}
			}
		} else if (oldExclusions != null) {
			ArrayList<String> exclusions = new ArrayList<String>();
			// if all were excluded then exclude all but this one
			if (oldExclusions.length == 1 && oldExclusions[0].equals("*")) {
				String[][] allFunctions = getAllFunctions(filePath);
				for (int i = 0; i < allFunctions.length; i++) {
					if (function[0].equals(allFunctions[i][0]))
						continue;
					exclusions.add(allFunctions[i][0]);
				}
			}
			else {
				for (String f : oldExclusions) {
					if (f.equals(function[0]))
						continue;
					exclusions.add(f);
				}
			}
			newExclusions = exclusions.toArray(new String[exclusions.size()]);
		}
		
		getExclusionsMap().remove(filePath);
		if (newExclusions != null) {
			getExclusionsMap().put(filePath, newExclusions);
		}
	}


	/**
	 * Gets the selection state of a function.
	 *
	 * @param filePath the path to the file defining the function
	 * @param function the function {name, expression, optional descriptor}
	 * @return true if the function is selected
	 */
	protected boolean isFunctionSelected(String filePath, String[] function) {
		String[] functions = getExclusionsMap().get(filePath);
		if (functions == null)
			return true;
		for (String name : functions) {
			if (name.equals("*")) //$NON-NLS-1$
				return false;
			if (name.equals(function[0]))
				return false;
		}
		return true;
	}

	/**
	 * Sets the selection state of a file.
	 *
	 * @param filePath the path to the file
	 * @param select   true to select the file
	 */
	protected void setFileSelected(String filePath, boolean select) {
		getExclusionsMap().remove(filePath); // no files excluded by default
		// if not selected, exclude all
		if (!select) {
			String[] function = new String[] { "*" }; //$NON-NLS-1$
			getExclusionsMap().put(filePath, function);
		}
	}

	/**
	 * Gets the exclusions map, mapping filePath to array of excluded function names.
	 */
	protected abstract Map<String, String[]> getExclusionsMap();

	/**
	 * Gets the selection state of a file.
	 *
	 * @param filePath the path to the file
	 * @return true if selected
	 */
	protected boolean isFileSelected(String filePath) {
		String[] functions = getExclusionsMap().get(filePath);
		if (functions == null || functions.length == 0) {
			// no exclusions
			return true;
		}
		if (functions[0].equals("*") || functions.length == getAllFunctions(filePath).length) { //$NON-NLS-1$
			// all excluded
			return false;
		}
		// some but not all excluded
		return true;
	}
	
	/**
	 * Refreshes the autoload data.
	 */
	protected abstract void refreshAutoloadData();

	/**
	 * Gets a  message in a Box that reports no functions found.
	 */
	private Box getEmptyMessage(int inset) {
		JLabel label = new JLabel(ToolsRes.getString("AutoloadManager.Label.NoFunctionsFound")); //$NON-NLS-1$
		label.setFont(lightFont);
		label.setBorder(BorderFactory.createEmptyBorder(2, inset, 4, 0));
		Box bar = Box.createHorizontalBox();
		bar.add(label);
		bar.add(Box.createHorizontalGlue());
		return bar;
	}

	/**
	 * A checkbox to indicate the status of an autoloadable function.
	 */
	private class AutoloadFunctionCheckbox extends JCheckBox {

		String[] function;
		AutoloadFileCheckbox fileCheckBox;

		/**
		 * Constructs a AutoloadFunctionCheckbox.
		 * 
		 * @param identifier the function identifier
		 */
		private AutoloadFunctionCheckbox(AutoloadFileCheckbox fileCheckbox, String[] f) {
			fileCheckBox = fileCheckbox;
			function = f;
			fileCheckBox.functionCheckBoxes.add(this);
			setSelected(isFunctionSelected(fileCheckBox.filePath, function));
			setText(f[0] + " = " + f[1]); //$NON-NLS-1$
			setIconTextGap(10);
			setOpaque(false);
			setToolTipText(ToolsRes.getString("AutoloadManager.FunctionCheckbox.Tooltip")); //$NON-NLS-1$
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean hasSelections = false;
					for (int i = 0; i < fileCheckBox.functionCheckBoxes.size(); i++) {
						hasSelections = hasSelections || fileCheckBox.functionCheckBoxes.get(i).isSelected();
					}
					if (hasSelections && !fileCheckBox.isSelected())
						fileCheckBox.setSelected(true);
					else if (!hasSelections && fileCheckBox.isSelected())
						fileCheckBox.setSelected(false);

					setFunctionSelected(fileCheckBox.filePath, function, AutoloadFunctionCheckbox.this.isSelected());
					repaint();
				}
			});
		}

	}

	/**
	 * A checkbox to indicate the status of an autoloadable file.
	 */
	private class AutoloadFileCheckbox extends JCheckBox {

		String fileName, filePath;
		ArrayList<AutoloadFunctionCheckbox> functionCheckBoxes = new ArrayList<AutoloadFunctionCheckbox>();

		/**
		 * Constructs a AutoloadFileCheckbox.
		 * 
		 * @param identifier the file identifier
		 */
		private AutoloadFileCheckbox(String dir, String name) {
			fileName = name;
			File file = new File(dir, fileName);
			filePath = XML.forwardSlash(file.getAbsolutePath());
			setText(fileName);
			setIconTextGap(10);
			setOpaque(false);
			setToolTipText(ToolsRes.getString("AutoloadManager.FileCheckbox.Tooltip")); //$NON-NLS-1$
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean select = AutoloadFileCheckbox.this.isSelected();
					setFileSelected(filePath, select);
					for (int i = 0; i < functionCheckBoxes.size(); i++) {
						functionCheckBoxes.get(i).setSelected(select);
					}
					repaint();
				}
			});
			
		}
		
	}

	/**
	 * A dialog to add and remove search paths
	 */
	protected class SearchPathDialog extends JDialog {

		HashSet<File> addedFiles = new HashSet<File>();
		TreeSet<String> directoryPaths = new TreeSet<String>();
		JButton okButton, addButton, removeButton;
		JList<String> directoryList;
		DefaultListModel<String> directoryListModel;

		/**
		 * Constructor
		 */
		SearchPathDialog() {
			super(AbstractAutoloadManager.this, true);
			createGUI();
			for (String dir : searchPaths) {
				File file = new File(dir);
				addedFiles.add(file);
			}
			refreshFileList();
		}

		/**
		 * Creates the GUI.
		 */
		private void createGUI() {
			JPanel contentPane = new JPanel(new BorderLayout());
			setContentPane(contentPane);

			// directory list
			directoryListModel = new DefaultListModel<>();
			directoryList = new JList<>(directoryListModel);
			directoryList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					String dir = directoryList.getSelectedValue();
					removeButton.setEnabled(dir != null);
				}
			});
			JScrollPane scroller = new JScrollPane(directoryList);
			scroller.setPreferredSize(new Dimension(300, 150));
			contentPane.add(scroller, BorderLayout.CENTER);

			// button bar
			JPanel buttonbar = new JPanel();
			addButton = new JButton();
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// show file chooser to add folders
					File file = chooseSearchDirectory(SearchPathDialog.this);
					if (file == null)
						return; // cancelled by user
					addedFiles.add(file);
					String path = XML.forwardSlash(file.getAbsolutePath());
					addSearchPath(path);
					refreshFileList();
					refreshAutoloadData();
				}
			});
			removeButton = new JButton();
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = directoryList.getSelectedValue();
					if (name != null) {
						for (Iterator<File> it = addedFiles.iterator(); it.hasNext();) {
							File next = it.next();
							String nextPath = XML.forwardSlash(next.getAbsolutePath());
							if (name.equals(nextPath)) {
								it.remove();
								searchPaths.remove(nextPath);
								break;
							}
						}
						refreshFileList();
						refreshAutoloadData();
					}
				}
			});
			okButton = new JButton();
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			buttonbar.add(addButton);
			buttonbar.add(removeButton);
			buttonbar.add(okButton);
			contentPane.add(buttonbar, BorderLayout.SOUTH);

			pack();
		}

		/**
		 * Uses a JFileChooser to select a search directory.
		 * 
		 * @param parent a component to own the file chooser
		 * @return the chosen file
		 */
		private File chooseSearchDirectory(Component parent) {
			JFileChooser chooser = new JFileChooser(searchPathChooserDir);
			if (OSPRuntime.isMac())
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			else
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			javax.swing.filechooser.FileFilter folderFilter = new javax.swing.filechooser.FileFilter() {
				// accept directories only
				@Override
				public boolean accept(File f) {
					if (f == null)
						return false;
					return f.isDirectory();
				}

				@Override
				public String getDescription() {
					return ToolsRes.getString("LibraryTreePanel.FolderFileFilter.Description"); //$NON-NLS-1$
				}
			};
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addChoosableFileFilter(folderFilter);
			String text = ToolsRes.getString("LibraryManager.Button.Add"); //$NON-NLS-1$
			chooser.setDialogTitle(text);
			FontSizer.setFonts(chooser, FontSizer.getLevel());
			int result = chooser.showDialog(parent, text);
			if (result == JFileChooser.APPROVE_OPTION) {
				searchPathChooserDir = chooser.getCurrentDirectory().getAbsolutePath();
				return chooser.getSelectedFile();
			}
			return null;
		}

		/**
		 * Refreshes the GUI.
		 */
		void refreshGUI() {
			setTitle(ToolsRes.getString("AutoloadManager.Button.SearchPaths")); //$NON-NLS-1$
			okButton.setText(ToolsRes.getString("Button.OK")); //$NON-NLS-1$
			addButton.setText(ToolsRes.getString("LibraryManager.Button.Add") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
			removeButton.setText(ToolsRes.getString("LibraryManager.Button.Remove")); //$NON-NLS-1$
			String dir = directoryList.getSelectedValue();
			removeButton.setEnabled(dir != null);
		}

		/**
		 * Refreshes the file list.
		 */
		void refreshFileList() {
			directoryListModel.clear();
			directoryPaths.clear();
			for (File next : addedFiles) {
				directoryPaths.add(XML.forwardSlash(next.getAbsolutePath()));
			}
			for (String next : directoryPaths) {
				directoryListModel.addElement(next);
			}
		}

	}

}
