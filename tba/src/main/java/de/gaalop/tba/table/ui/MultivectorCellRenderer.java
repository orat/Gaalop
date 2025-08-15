package de.gaalop.tba.table.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author Oliver Rettig (Oliver.Rettig@orat.de)
 */
public class MultivectorCellRenderer implements TableCellRenderer {

    private static Color SCALAR_ZERO_COLOR = new Color(0.933f, 1f, 0.933f);
    private static Color SCALAR_POSITIVE_COLOR = new Color(0.533f, 1f, 0.533f);
    private static Color SCALAR_NEGATIVE_COLOR = new Color(1f, 0.533f, 0.533f);
    private static Color VECTOR_POSITIVE_COLOR = new Color(0.80f,0.80f,1f);
    private static Color VECTOR_NEGATIVE_COLOR = new Color(0.667f,0.667f,1f);
    private static Color BIVECTOR_POSITIVE_COLOR = new Color(1f,0.80f,0.80f);
    private static Color BIVECTOR_NEGATIVE_COLOR = new Color(1f,0.667f,0.667f);
    private static Color TRIVECTOR_POSITIVE_COLOR = new Color(1f,0.80f,1f);
    private static Color TRIVECTOR_NEGATIVE_COLOR = new Color(1f,0.667f,1f);
    private static Color QUATVECTOR_POSITIVE_COLOR = new Color(0.80f,1f,1f);
    private static Color QUATVECTOR_NEGATIVE_COLOR = new Color(0.533f,1f,1f);
    private static Color QUINTVECTOR_POSITIVE_COLOR = new Color(1f,1f,0.80f);
    private static Color QUINTVECTOR_NEGATIVE_COLOR = new Color(1f,1f,0.533f);
  
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        MultivectorCell cell = (MultivectorCell) table.getValueAt(row, column);
         // sign makes sense only if there is no sum of blades
        JLabel comp = new JLabel();
        comp.setOpaque(true);
        comp.setBackground(getColor(cell.getGrade(), cell.isNegative()));
        MultivectorJTableModel tableModel = (MultivectorJTableModel) table.getModel();
        comp.setText(cell.toString(tableModel.getBasisBladeNames()));
        comp.setHorizontalAlignment(JLabel.CENTER);
        return comp;
    }
    
    private static Color getColor(int grade, boolean isNegative){
        Color result = SCALAR_ZERO_COLOR;
        if (!isNegative){
            switch (grade){
                case 0:
                    result = SCALAR_POSITIVE_COLOR;
                    break;
                case 1:
                    result = VECTOR_POSITIVE_COLOR;
                    break;
                case 2:
                    result = BIVECTOR_POSITIVE_COLOR;
                    break;
                case 3:
                    result = TRIVECTOR_POSITIVE_COLOR;
                    break;
                case 4:
                    result = QUATVECTOR_POSITIVE_COLOR;
                    break;
                case 5:
                    result = QUINTVECTOR_POSITIVE_COLOR;
                    break;
                default:
            }
        } else {
             switch (grade){
                case 0:
                    result = SCALAR_NEGATIVE_COLOR;
                    break;
                case 1:
                    result = VECTOR_NEGATIVE_COLOR;
                    break;
                case 2:
                    result = BIVECTOR_NEGATIVE_COLOR;
                    break;
                case 3:
                    result = TRIVECTOR_NEGATIVE_COLOR;
                    break;
                case 4:
                    result = QUATVECTOR_NEGATIVE_COLOR;
                    break;
                case 5:
                    result = QUINTVECTOR_NEGATIVE_COLOR;
                    break;
                default:
             }
        }
        return result;
    }
}