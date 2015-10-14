/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.sewol.context.du;

import de.uni.freiburg.iig.telematik.sewol.context.process.ProcessContext;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.LEFT;

/**
 *
 * @author stocker
 */
public class DataUsageListRenderer extends JLabel implements ListCellRenderer {

        private final ProcessContext context;

        public DataUsageListRenderer(ProcessContext context) {
            this.context = context;
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            boolean activityHasDataUsage = false;
            try {
                activityHasDataUsage = !context.getDataUsageFor(value.toString()).isEmpty();
            } catch (Exception e) {
            }

//			if(activityHasDataUsage){
//				setText("<html><b>"+(String) value+"");
//			} else {
//				setText((String) value);
//			}
            setText((String) value);
            setToolTipText((String) value);

            if (isSelected) {
                setBackground(new Color(10, 100, 200));
                setForeground(new Color(0, 0, 0));
            } else {
                if ((index % 2) == 0) {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                } else {
                    setBackground(new Color(230, 230, 230));
                    setForeground(list.getForeground());
                }
                if (activityHasDataUsage) {
                    setBackground(new Color(201, 233, 255));
                }
            }

            return this;

        }

    }
