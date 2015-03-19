package com.github.javacliparser.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.github.javacliparser.ListOption;
import com.github.javacliparser.Option;

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
	
	private Map<Option, JComponent> dynamicEditComponents = new HashMap<Option, JComponent>();
	
	public ListOptionEditComponent(Option opt) {
	
		// TODO: remove try/catch when entire pipeline is complete.
		try {
		
		ListOption option = (ListOption)opt;
		
		editedOption = option;
		
		for(Option listopt : option.getList()) {
			addOption(listopt);
		}
		
		scrollPane = new JScrollPane(optionsPanel);
		scrollPane.setPreferredSize(new Dimension(250,150));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		this.setLayout(new BorderLayout());
		
		this.add(scrollPane, BorderLayout.NORTH);
		this.add(addButton, BorderLayout.SOUTH);
		
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));

		this.addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Add the specified default option, user can then edit it.
				addOption(editedOption.getExpectedType().copy());
			}
		});
		
		
		}
		catch (Throwable ex)
		{
			System.out.println(ex);
		}

	}
	
	private void addOption(final Option opt) {

		System.out.println(opt.getValueAsCLIString() + opt.getName());
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
    	dynamicEditComponents.put(opt, editComponent);
        optionsPanel.add(editComponent);
		
        refreshOptionsPanel();
	}
	
	private void removeOption(Option opt) {
		optionsPanel.remove(dynamicEditComponents.get(opt));
		dynamicEditComponents.remove(opt);
		refreshOptionsPanel();
	}
	
	private void refreshOptionsPanel() {
		optionsPanel.revalidate();
		optionsPanel.repaint();
	}
	
	@Override
	public Option getEditedOption() {
		
		return editedOption;
	}

	@Override
	public void setEditState(String cliString) {
		editedOption.setValueViaCLIString(cliString);
	}

	@Override
	public void applyState() {
		Option[] options = dynamicEditComponents.keySet().toArray(new Option[0]);
		
		for(JComponent component : dynamicEditComponents.values()) {
			OptionEditComponent editComponent = (OptionEditComponent)component;
			editComponent.applyState();
		}
		
		editedOption.setList(options);
	}

}
