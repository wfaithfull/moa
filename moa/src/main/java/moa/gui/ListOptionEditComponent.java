package moa.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import moa.options.ClassOption;
import moa.streams.generators.cd.ConceptDriftGenerator;
import moa.streams.generators.cd.NoChangeGenerator;

import com.github.javacliparser.IntOption;
import com.github.javacliparser.ListOption;
import com.github.javacliparser.Option;
import com.github.javacliparser.Options;
import com.github.javacliparser.gui.OptionEditComponent;
import com.github.javacliparser.gui.OptionsConfigurationPanel;

public class ListOptionEditComponent extends JPanel implements
		OptionEditComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 760469752324414134L;

	protected ListOption editedOption;

	protected JButton addButton = new JButton("Add");
	
	protected JScrollPane scrollPane;
	
	protected JPanel optionsPanel = new JPanel();
	
	private Map<Option, JComponent> dynamicButtons = new HashMap<Option, JComponent>();
	
	public ListOptionEditComponent(Option opt) {
		
		try {
		
		System.out.println(opt.getName());
		ListOption option = (ListOption)opt;
		
		editedOption = option;
		
		scrollPane = new JScrollPane(optionsPanel);
		scrollPane.setPreferredSize(new Dimension(250,150));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//scrollPane.setBounds(0,0,100,100);
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		this.add(scrollPane);
		this.add(addButton);
		
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
		//init();

		this.addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addOption(new ClassOption("gen1", 'c', "purpose", ConceptDriftGenerator.class, "NoChangeGenerator" ));
			}
		});
		
		
		}
		catch (Throwable ex)
		{
			System.out.println(ex);
		}

	}
	
	private void addOption(final Option opt) {
		
		Option[] curOpt = editedOption.getList();
		Option[] newOpt = Arrays.copyOf(curOpt, curOpt.length + 1);
		newOpt[newOpt.length-1] = opt;
		editedOption.setList(newOpt);
		
		JComponent editComponent = OptionsConfigurationPanel.getEditComponent(opt);
    	
    	JButton delButton = new JButton("X");
    	
    	delButton.addActionListener(new ActionListener() {
    		@Override 
    		public void actionPerformed(ActionEvent e) {
    			removeOption(opt);
    		}
    		
    	});
    	
    	
    	
    	editComponent.add(delButton, BorderLayout.WEST);
    	editComponent.setMaximumSize(new Dimension(300, 20));
    	dynamicButtons.put(opt, editComponent);
        optionsPanel.add(editComponent);
		
        refreshOptionsPanel();
	}
	
	private void removeOption(Option opt) {
		optionsPanel.remove(dynamicButtons.get(opt));
		dynamicButtons.remove(opt);
		refreshOptionsPanel();
		
	}
	
	private void refreshOptionsPanel() {
		optionsPanel.revalidate();
		optionsPanel.repaint();
	}
	
	@Override
	public Option getEditedOption() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEditState(String cliString) {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyState() {
		// TODO Auto-generated method stub

	}

}
