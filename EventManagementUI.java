package event;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class EventManagementUI {

    JFrame frame;
    Connection con;

    // Database Credentials
    private static final String URL = "jdbc:mysql://localhost:3306/event";
    private static final String USER = "root";
    private static final String PASS = "dbms"; 

    public EventManagementUI() {
        frame = new JFrame("Event Management System");
        con = connectDB();

        if (con == null) {
            JOptionPane.showMessageDialog(null, "Database Connection Failed! \nCheck XAMPP and Password.");
            System.exit(0);
        }

        JTabbedPane tab = new JTabbedPane();
        tab.add("Events Management", eventPanel());
        tab.add("Attendees Registry", attendeePanel());

        frame.add(tab);
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private Connection connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            System.out.println("❌ Connection Error: " + e.getMessage());
            return null;
        }
    }

    // ================= EVENTS PANEL =================
    JPanel eventPanel() {
        JTextField idField = new JTextField(); 
        idField.setEditable(true); // User must enter ID manually
        
        JTextField name = new JTextField(), date = new JTextField(), loc = new JTextField(), cap = new JTextField();
        JTextArea desc = new JTextArea(3, 20);
        
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Date", "Location", "Capacity", "Description"});
        JTable table = new JTable(model);

        JButton add = new JButton("Add");
        JButton upd = new JButton("Update");
        JButton del = new JButton("Delete");
        JButton view = new JButton("View/Refresh");

        JPanel input = new JPanel(new GridLayout(6, 2, 10, 10));
        input.setBorder(BorderFactory.createTitledBorder("Event Details (Manual ID Required)"));
        input.add(new JLabel("Event ID:")); input.add(idField);
        input.add(new JLabel("Event Name:")); input.add(name);
        input.add(new JLabel("Date (YYYY-MM-DD):")); input.add(date);
        input.add(new JLabel("Location:")); input.add(loc);
        input.add(new JLabel("Max Capacity:")); input.add(cap);
        input.add(new JLabel("Description:")); input.add(new JScrollPane(desc));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(input, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel();
        btnPanel.add(add); btnPanel.add(upd); btnPanel.add(del); btnPanel.add(view);
        panel.add(btnPanel, BorderLayout.SOUTH);

        Runnable load = () -> {
            try {
                model.setRowCount(0);
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT event_id, event_name, event_date, location, max_capacity, description FROM Events");
                while(rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5), rs.getString(6)});
                }
            } catch(Exception e) { e.printStackTrace(); }
        };

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                idField.setText(model.getValueAt(row, 0).toString());
                name.setText(model.getValueAt(row, 1).toString());
                date.setText(model.getValueAt(row, 2).toString());
                loc.setText(model.getValueAt(row, 3).toString());
                cap.setText(model.getValueAt(row, 4).toString());
                Object d = model.getValueAt(row, 5);
                desc.setText(d != null ? d.toString() : "");
            }
        });

        // ADD LOGIC (MANUAL ID)
        add.addActionListener(e -> {
            try {
                String sql = "INSERT INTO Events (event_id, event_name, event_date, location, description, max_capacity) VALUES (?,?,?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(idField.getText()));
                pst.setString(2, name.getText());
                pst.setString(3, date.getText());
                pst.setString(4, loc.getText());
                pst.setString(5, desc.getText());
                pst.setInt(6, Integer.parseInt(cap.getText()));
                pst.executeUpdate();
                load.run();
                JOptionPane.showMessageDialog(frame, "✅ Event Added!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "❌ Error: Ensure ID is unique! " + ex.getMessage()); }
        });

        // UPDATE LOGIC (MANUAL ID)
        upd.addActionListener(e -> {
            try {
                String sql = "UPDATE Events SET event_name=?, event_date=?, location=?, description=?, max_capacity=? WHERE event_id=?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, name.getText());
                pst.setString(2, date.getText());
                pst.setString(3, loc.getText());
                pst.setString(4, desc.getText());
                pst.setInt(5, Integer.parseInt(cap.getText()));
                pst.setInt(6, Integer.parseInt(idField.getText()));
                int rows = pst.executeUpdate();
                if(rows > 0) { load.run(); JOptionPane.showMessageDialog(frame, "🔄 Updated!"); }
                else { JOptionPane.showMessageDialog(frame, "⚠️ ID not found."); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "❌ Error: " + ex.getMessage()); }
        });

        view.addActionListener(e -> load.run());

        del.addActionListener(e -> {
            try {
                PreparedStatement pst = con.prepareStatement("DELETE FROM Events WHERE event_id=?");
                pst.setInt(1, Integer.parseInt(idField.getText()));
                int rows = pst.executeUpdate();
                if(rows > 0) { load.run(); JOptionPane.showMessageDialog(frame, "🗑️ Deleted!"); }
                else { JOptionPane.showMessageDialog(frame, "⚠️ ID not found!"); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "❌ Error: " + ex.getMessage()); }
        });

        load.run();
        return panel;
    }

    // ================= ATTENDEE PANEL =================
    JPanel attendeePanel() {
        JTextField aIdField = new JTextField(); 
        aIdField.setEditable(true); // User must enter ID manually
        
        JTextField aName = new JTextField(), email = new JTextField(), evId = new JTextField();
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.setColumnIdentifiers(new String[]{"ID", "Name", "Email", "Event ID"});
        JTable table = new JTable(model);
        
        JButton add = new JButton("Register");
        JButton upd = new JButton("Update");
        JButton del = new JButton("Delete");
        JButton view = new JButton("View/Refresh");

        JPanel input = new JPanel(new GridLayout(4, 2, 10, 10));
        input.setBorder(BorderFactory.createTitledBorder("Attendee Details (Manual ID Required)"));
        input.add(new JLabel("Attendee ID:")); input.add(aIdField);
        input.add(new JLabel("Full Name:")); input.add(aName);
        input.add(new JLabel("Email:")); input.add(email);
        input.add(new JLabel("Event ID:")); input.add(evId);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(input, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel();
        btnPanel.add(add); btnPanel.add(upd); btnPanel.add(del); btnPanel.add(view);
        panel.add(btnPanel, BorderLayout.SOUTH);

        Runnable load = () -> {
            try {
                model.setRowCount(0);
                ResultSet rs = con.createStatement().executeQuery("SELECT attendee_id, full_name, email, event_id FROM Attendees");
                while(rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)});
            } catch(Exception e) { e.printStackTrace(); }
        };

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                aIdField.setText(model.getValueAt(row, 0).toString());
                aName.setText(model.getValueAt(row, 1).toString());
                email.setText(model.getValueAt(row, 2).toString());
                evId.setText(model.getValueAt(row, 3).toString());
            }
        });

        // ADD ATTENDEE (MANUAL ID)
        add.addActionListener(e -> {
            try {
                String sql = "INSERT INTO Attendees (attendee_id, full_name, email, event_id) VALUES (?,?,?,?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, Integer.parseInt(aIdField.getText()));
                pst.setString(2, aName.getText());
                pst.setString(3, email.getText());
                pst.setInt(4, Integer.parseInt(evId.getText()));
                pst.executeUpdate();
                load.run();
                JOptionPane.showMessageDialog(frame, "✅ Registered!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "❌ Error! Ensure ID is unique and Event ID exists."); }
        });

        upd.addActionListener(e -> {
            try {
                PreparedStatement pst = con.prepareStatement("UPDATE Attendees SET full_name=?, email=?, event_id=? WHERE attendee_id=?");
                pst.setString(1, aName.getText()); 
                pst.setString(2, email.getText());
                pst.setInt(3, Integer.parseInt(evId.getText())); 
                pst.setInt(4, Integer.parseInt(aIdField.getText()));
                int rows = pst.executeUpdate();
                if(rows > 0) { load.run(); JOptionPane.showMessageDialog(frame, "🔄 Updated!"); }
                else { JOptionPane.showMessageDialog(frame, "⚠️ ID not found."); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "❌ Error!"); }
        });

        del.addActionListener(e -> {
            try {
                PreparedStatement pst = con.prepareStatement("DELETE FROM Attendees WHERE attendee_id=?");
                pst.setInt(1, Integer.parseInt(aIdField.getText()));
                int rows = pst.executeUpdate();
                if(rows > 0) { load.run(); JOptionPane.showMessageDialog(frame, "🗑️ Deleted!"); }
                else { JOptionPane.showMessageDialog(frame, "⚠️ ID not found!"); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, "❌ Error!"); }
        });

        view.addActionListener(e -> load.run());

        load.run();
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventManagementUI());
    }
}