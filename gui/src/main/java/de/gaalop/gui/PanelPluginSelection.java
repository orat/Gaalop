package de.gaalop.gui;

import de.gaalop.*;
import de.gaalop.algebra.DefinedAlgebra;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.apache.commons.beanutils.BeanUtils;

/**
 * Right side of the user interface - mainly with a list of comoboboxe to choose
 * main functionality of the code generation process.
 * 
 * @author Christian Steinmetz
 */
public class PanelPluginSelection extends JPanel {
    
    private JComboBox algebraChooser = new JComboBox();
    private JSpinner dimensionSpinner;
    private JComboBox globalSettings;       // only if more than one
    private JComboBox visualCodeInserter;   // visual code inserter       
    private JComboBox algebra;              // algebra strategy onle if more than one
    private JComboBox optimization;         // optimization
    private JComboBox generator;            // code generator
    
    private String errorMessage;
    private String errorPlugin1;
    private String errorPlugin2;
    
    private Color defaultColor;
    
    private JTextArea errorTextArea = new JTextArea();
    
    
    public static String lastUsedAlgebra;
    public static boolean lastUsedAlgebraRessource;
    public static int lastUsedAlgebraDimension;
    
    public static String lastUsedGenerator;
    public static String lastUsedVisualCodeInserter;
    public static String lastUsedOptimization;
    
     
    private void updateErrorMessage(){
        if (areConstraintsFulfilled()) {
            errorTextArea.setText("");
            setColorOnAllComboBoxes();
        } else {
            errorTextArea.setText(errorMessage);
            setColorOnAllComboBoxes();
        }
    }
    
    private ItemListener itemListener = (ItemEvent e) -> {
        updateErrorMessage();
    };
   
    private ItemListener algebraItemListener = (ItemEvent e) -> {
        updateDimensionSpinner();
        //updateErrorMessage();
    };
    
    private boolean isErrorPlugin(JComboBox comboBox) {
        String name = comboBox.getSelectedItem().getClass().getCanonicalName();
        return name.equals(errorPlugin1) || name.equals(errorPlugin2);
    }
    
    private void setColorOnAllComboBoxes() {
        if (isErrorPlugin(visualCodeInserter))
            visualCodeInserter.setBackground(Color.red);
        else
            visualCodeInserter.setBackground(defaultColor);
        
        if (isErrorPlugin(algebra))
            algebra.setBackground(Color.red); 
        else
            algebra.setBackground(defaultColor);
        
        if (isErrorPlugin(optimization))
            optimization.setBackground(Color.red); 
        else
            optimization.setBackground(defaultColor);
        
        if (isErrorPlugin(generator))
            generator.setBackground(Color.red); 
        else
            generator.setBackground(defaultColor);
    }
    
    public PanelPluginSelection() {
        final Font font = new Font("Arial", Font.PLAIN, FontSize.getGuiFontSize());
        setLayout(new GridLayout(7,1,5,5));
        
        // cell renderer for the combobox entries
        ListCellRenderer c = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String valueStr = ((Plugin) value).getName();
                JLabel label = (JLabel) super.getListCellRendererComponent(list, valueStr, index, isSelected, cellHasFocus);
                label.setFont(font);
                Image icon = (((Plugin) value).getIcon());
                if (icon != null)
                    label.setIcon(new ImageIcon(((Plugin) value).getIcon()));
                return label ;
            }
        };
        
        //Why?
        add(new JPanel());

        // 1. algebra chooser
        algebraChooser.setFont(font);
        algebraChooser.addItemListener(algebraItemListener);
        JPanel algebraChooser2 = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbl.setConstraints(algebraChooser2, gbc);
        algebraChooser2.setLayout(gbl);
    
        // create a JSpinner to input the optional algebra dimension
        // Nummern-Spinner für Werte von 1 to 10, in 1 Schritte
        int maxdim = 10; //TODO where to define this, depending from the algebra
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel( 2, 0, maxdim, 1 );
        dimensionSpinner = new JSpinner(spinnerModel);
        dimensionSpinner.setEnabled(false);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        algebraChooser2.add(algebraChooser, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        algebraChooser2.add(dimensionSpinner);
                
        addLabeledComponent("Algebra to use:", algebraChooser2);
        
        
        PluginSorter comparator = new PluginSorter();
        
        // 2. global settings strategy plugins (is there more than one at the moment?)
        GlobalSettingsStrategyPlugin[] globalPlugins = Plugins.getGlobalSettingsStrategyPlugins().toArray(new GlobalSettingsStrategyPlugin[0]);
        Arrays.sort(globalPlugins, comparator);
        globalSettings = new JComboBox(globalPlugins);
        globalSettings.setFont(font);
        globalSettings.setSelectedItem(search(globalPlugins, "de.gaalop.globalSettings.Plugin"));
        globalSettings.addItemListener(itemListener);
        globalSettings.setRenderer(c);
        if (Plugins.getGlobalSettingsStrategyPlugins().size() > 1)
            addLabeledComponent("Global Settings Plugin:", globalSettings);
        
        // 3. visual code inserter plugins
        VisualCodeInserterStrategyPlugin[] visPlugins = Plugins.getVisualizerStrategyPlugins().toArray(new VisualCodeInserterStrategyPlugin[0]);
        Arrays.sort(visPlugins, comparator);
        visualCodeInserter = new JComboBox(visPlugins);
        visualCodeInserter.setFont(font);
        visualCodeInserter.setSelectedItem(search(visPlugins, "de.gaalop.visualCodeInserter.Plugin"));
        visualCodeInserter.addItemListener(itemListener);
        visualCodeInserter.setRenderer(c);
        if (Plugins.getVisualizerStrategyPlugins().size() > 1)
            addLabeledComponent("VisualCodeInserter:", visualCodeInserter);
        
        // 4. algebra stategy plugins (is there more than one at the moment?)
        AlgebraStrategyPlugin[] algPlugins = Plugins.getAlgebraStrategyPlugins().toArray(new AlgebraStrategyPlugin[0]);
        Arrays.sort(algPlugins, comparator);
        algebra = new JComboBox(algPlugins);
        algebra.setSelectedItem(search(algPlugins, "de.gaalop.algebra.Plugin"));
        algebra.addItemListener(itemListener);
        algebra.setFont(font);
        algebra.setRenderer(c);
        if (Plugins.getAlgebraStrategyPlugins().size() > 1)
            addLabeledComponent("Algebra:", algebra);
        
        // 5. optimization
        OptimizationStrategyPlugin[] optPlugins = Plugins.getOptimizationStrategyPlugins().
                toArray(new OptimizationStrategyPlugin[0]);
        Arrays.sort(optPlugins, comparator);
        optimization = new JComboBox(optPlugins);
        optimization.setFont(font);
        optimization.setSelectedItem(search(optPlugins, "de.gaalop.tba.Plugin"));
        optimization.addItemListener(itemListener);
        optimization.setRenderer(c);
        if (Plugins.getOptimizationStrategyPlugins().size() > 1)
            addLabeledComponent("Optimization:", optimization);
        
        // 6. code generator plugins
        CodeGeneratorPlugin[] codegenPlugins = Plugins.getCodeGeneratorPlugins().
                toArray(new CodeGeneratorPlugin[0]);
        Arrays.sort(codegenPlugins, comparator);
        generator = new JComboBox(codegenPlugins);
        generator.setFont(font);
        generator.addItemListener(itemListener);
        generator.setRenderer(c);
        if (Plugins.getCodeGeneratorPlugins().size() > 1)
            addLabeledComponent("CodeGenerator:", generator);
        
        // 7. error text area 
        errorTextArea.setLineWrap(true);
        errorTextArea.setWrapStyleWord(true);
        errorTextArea.setBackground(getBackground());
        add(errorTextArea);
        defaultColor = generator.getBackground();
    }
    
    private void addLabeledComponent(String label, JComponent /*JComboBox*/ c) {
        Font font = new Font("Arial", Font.PLAIN, FontSize.getGuiFontSize());
        JPanel panel = new JPanel(new GridLayout(2,1));
        JLabel l = new JLabel(label);
        l.setFont(font);
        panel.add(l);
        panel.add(c);
        add(panel);
    }
    
    
    // API to get the chooses plugins 
    
    public GlobalSettingsStrategyPlugin getGlobalSettingsStrategyPlugin() {
        return (GlobalSettingsStrategyPlugin) globalSettings.getSelectedItem();
    }
    
    public VisualCodeInserterStrategyPlugin getVisualizerStrategyPlugin() {
        return (VisualCodeInserterStrategyPlugin) visualCodeInserter.getSelectedItem();
    }
    
    public AlgebraStrategyPlugin getAlgebraStrategyPlugin() {
        return (AlgebraStrategyPlugin) algebra.getSelectedItem();
    }
    
    public OptimizationStrategyPlugin getOptimizationStrategyPlugin() {
        return (OptimizationStrategyPlugin) optimization.getSelectedItem();
    }
    
    public CodeGeneratorPlugin getCodeGeneratorPlugin() {
        return (CodeGeneratorPlugin) generator.getSelectedItem();
    }

    
    private Object search(Plugin[] plugins, String search) {
        for (Plugin p: plugins) 
            if (p.getClass().getCanonicalName().equals(search))
                return p;
        
        if (plugins.length>0)
            return plugins[0];
        
        return null;
    }
    
    public boolean areConstraintsFulfilled() {
        errorMessage = "";
        errorPlugin1 = null;
        errorPlugin2 = null;
        LinkedList<Plugin> plugins = new LinkedList<>();
        plugins.add(getVisualizerStrategyPlugin());
        plugins.add(getAlgebraStrategyPlugin());
        plugins.add(getOptimizationStrategyPlugin());
        plugins.add(getCodeGeneratorPlugin());
        
        if (!depends(
                "de.gaalop.codegenGapp.Plugin",
                "de.gaalop.gapp.Plugin",
                "The GAPPCodeGenerator needs the GAPP Optimizer!", 
                plugins)) 
            return false;
        if (!depends(
                "de.gaalop.gappopencl.Plugin",
                "de.gaalop.gapp.Plugin",
                "The GAPPOpenCL codeGenerator needs the GAPP Optimizer!",
                plugins)) 
            return false;
        if (!depends(
                "de.gaalop.vis2d.Plugin",
                "de.gaalop.visualCodeInserter2d.Plugin",
                "The Vis2d plugin needs the visualCodeInserter2d plugin!",
                plugins))
            return false;
        if (!depends(
                "de.gaalop.visualizer.Plugin",
                "de.gaalop.visualCodeInserter.Plugin",
                "The Visualizer plugin needs the visualCodeInserter plugin!",
                plugins)) 
            return false;
        if (!depends(
                "de.gaalop.gappDebugger.Plugin",
                "de.gaalop.gapp.Plugin",
                "The GAPP debugger needs the GAPP Optimizer!",
                plugins)) 
            return false;
        return true;
    }

    private boolean contains(String plugin, LinkedList<Plugin> plugins) {
        for (Plugin p: plugins) 
            if (p.getClass().getCanonicalName().equals(plugin))
                return true;
        
        return false;
    }
    
    private boolean depends(String plugin, String needs, String message, LinkedList<Plugin> plugins) {
        if (contains(plugin, plugins))
            if (!contains(needs, plugins)) {
                errorMessage = message;
                errorPlugin1 = needs;
                errorPlugin2 = plugin;
                return false;
            }
        return true;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void refreshAlgebras() {
        
        // visual code inserter
        VisualCodeInserterStrategyPlugin[] visualCodeInserterStrategyPlugins = 
                Plugins.getVisualizerStrategyPlugins().toArray(new VisualCodeInserterStrategyPlugin[0]);
        visualCodeInserter.setSelectedItem(search(visualCodeInserterStrategyPlugins, lastUsedVisualCodeInserter));
        
        // optimization
        OptimizationStrategyPlugin[] optimizationPlugins = 
                Plugins.getOptimizationStrategyPlugins().toArray(new OptimizationStrategyPlugin[0]);
        optimization.setSelectedItem(search(optimizationPlugins, lastUsedOptimization));
        
        // codegenerator
        CodeGeneratorPlugin[] codegenPlugins = Plugins.getCodeGeneratorPlugins().toArray(new CodeGeneratorPlugin[0]);
        generator.setSelectedItem(search(codegenPlugins, lastUsedGenerator));
        
         
        // algebras
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        
        // only ids of the algebras, definition file must not exist
        for (DefinedAlgebra definedAlgebra: de.gaalop.algebra.Plugin.getDefinedAlgebras()){
            boolean hasDimension = definedAlgebra.definesDimensions();
            model.addElement(new AlgebraChooserItem(true, definedAlgebra.id, hasDimension,
                    definedAlgebra.id+" - "+definedAlgebra.name));
        }
        
        // algebra strategy
        
        AlgebraStrategyPlugin algebra = Plugins.getAlgebraStrategyPlugins().iterator().next();
        
        // add user defined algebras, if there are any
        try {
             Field field = algebra.getClass().getField("additionalBaseDirectory");
             String value = BeanUtils.getProperty(algebra, field.getName()).trim();
             
             if (!value.isEmpty()) {
                File file = new File(value);
                File[] dirs = file.listFiles((File pathname) -> pathname.isDirectory());
                if (dirs != null)
                for (File dir: dirs) 
                    model.addElement(new AlgebraChooserItem(false, 
                            dir.getName(), false, "Own - "+dir.getName()));

                }
             
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(PanelPluginSelection.class.getName()).log(Level.SEVERE, null, ex);
        }
        algebraChooser.setModel(model);
        
        // default algebra is cga
        if (lastUsedAlgebra == null)  {
            lastUsedAlgebra = "cga";
            lastUsedAlgebraRessource = true;
            lastUsedAlgebraDimension = -1;
        }
        
        //TODO how to save a dimension for default item?
        boolean hasDimension = false;
        if (lastUsedAlgebraDimension > 0) hasDimension = true;
        AlgebraChooserItem defaultItem = new AlgebraChooserItem(lastUsedAlgebraRessource, 
                lastUsedAlgebra, hasDimension, "");
        
        // set the selection to the default item if the saved default item exist in the list of the combobox
        FOR:
        for (int i=0;i<model.getSize();i++) {
            AlgebraChooserItem iItem = (AlgebraChooserItem) model.getElementAt(i);
            if (defaultItem.algebraName.equals(iItem.algebraName) && 
                defaultItem.ressource == iItem.ressource) {
                defaultItem = iItem;
                break FOR;
            }
        }
        algebraChooser.setSelectedItem(defaultItem);
        try {
            SwingUtilities.invokeAndWait(new Runnable(){
                public void run(){
                    updateDimensionSpinner();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(PanelPluginSelection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(PanelPluginSelection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //spinner updaten
    private void updateDimensionSpinner(){
        AlgebraChooserItem selectedAlgebraItem = (AlgebraChooserItem) algebraChooser.getSelectedItem();
        //if (selectedAlgebraItem.dimension>0){
            for (DefinedAlgebra definedAlgebra: de.gaalop.algebra.Plugin.getDefinedAlgebras()){
                if (definedAlgebra.id.equals(selectedAlgebraItem.algebraName)){
                    if (definedAlgebra.definesDimensions()){
                        SpinnerNumberModel spinnerModel = 
                                new SpinnerNumberModel( definedAlgebra.minDimension, 
                                        definedAlgebra.minDimension, definedAlgebra.maxDimension, 1 );
                        dimensionSpinner.setModel(spinnerModel);
                        dimensionSpinner.setEnabled(true);
            
                    } else {
                        dimensionSpinner.setEnabled(false);
                        //System.out.println("Error: Selected algebra with dimension, but definedAlgebra has no dimension!");
                    }
                }
            }
        //} else {
        //     dimensionSpinner.setEnabled(false);
        //}
    }
    
    ChoosenAlgebra /*AlgebraChooserItem*/ getAlgebraToUse() {
        AlgebraChooserItem item = (AlgebraChooserItem) algebraChooser.getSelectedItem();
        //return (AlgebraChooserItem) algebraChooser.getSelectedItem();
        boolean resource = item.ressource;
        String algebraName = item.algebraName; 
        int dimension = 0; //TODO add dimension from the spinner UI if available
        return new ChoosenAlgebra(resource, algebraName, dimension);
    }

    public void updateLastUsedPlugins() {
        lastUsedGenerator = generator.getSelectedItem().getClass().getCanonicalName();
        lastUsedVisualCodeInserter = visualCodeInserter.getSelectedItem().getClass().getCanonicalName();
        lastUsedOptimization = optimization.getSelectedItem().getClass().getCanonicalName();
    }

}
