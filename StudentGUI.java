

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;

class Student implements Serializable {
    String id, name, dob, classId;
    Student(String id, String name, String dob, String classId) {
        this.id = id;
        this.name = name;
        this.dob = dob;
        this.classId = classId;
    }
}

class LoginFrame extends JFrame {
    static String adminUser = "Admin";
    static String adminPass = "2006";

    public LoginFrame() {
        setTitle("Login");
        setSize(350, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10,10,10,10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();

        g.gridx=0; g.gridy=0; add(new JLabel("Username:"), g);
        g.gridx=1; add(user, g);

        g.gridx=0; g.gridy=1; add(new JLabel("Password:"), g);
        g.gridx=1; add(pass, g);

        JButton btn = new JButton("Login");
        g.gridx=0; g.gridy=2; g.gridwidth=2;
        add(btn, g);

        btn.addActionListener(e -> {
            if(user.getText().equals(adminUser) && new String(pass.getPassword()).equals(adminPass)){
                new StudentGUI().setVisible(true);
                dispose();
            } else JOptionPane.showMessageDialog(this,"Sai tài khoản!");
        });
    }
}

public class StudentGUI extends JFrame {

    ArrayList<Student> list = new ArrayList<>();
    DefaultTableModel model;
    JTable table;

    JTextField txtName, txtClass, search;
    JSpinner txtDob;

    boolean isDark = false;
    final String FILE = "students.dat";

    // ===== ID =====
    String generateID(){
        int max = 0;
        for(Student s : list){
            try{
                int num = Integer.parseInt(s.id.replace("SV",""));
                if(num > max) max = num;
            }catch(Exception e){}
        }
        return String.format("SV%03d", max + 1);
    }

    public StudentGUI(){
        setTitle("Student Manager");
        setSize(1000,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        load();

        // ===== TOP =====
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel(" Student Manager");

        JButton darkBtn = new JButton("🌙");
        darkBtn.addActionListener(e->{
            try{
                if(!isDark){ UIManager.setLookAndFeel(new FlatDarkLaf()); darkBtn.setText("☀"); }
                else { UIManager.setLookAndFeel(new FlatLightLaf()); darkBtn.setText("🌙"); }
                isDark=!isDark;
                SwingUtilities.updateComponentTreeUI(this);
            }catch(Exception ex){}
        });

        JButton logout = new JButton("Logout");
        logout.addActionListener(e->{dispose(); new LoginFrame().setVisible(true);});

        JPanel right = new JPanel();
        right.add(darkBtn);
        right.add(logout);

        top.add(title,BorderLayout.WEST);
        top.add(right,BorderLayout.EAST);
        add(top,BorderLayout.NORTH);

        // ===== SIDEBAR =====
        JPanel side = new JPanel(new GridLayout(6,1,10,10));
        JButton home = new JButton("🏠 Dashboard");
        JButton manage = new JButton("🎓 Students");
        side.add(home);
        side.add(manage);
        add(side,BorderLayout.WEST);

        // ===== CONTENT =====
        CardLayout cl = new CardLayout();
        JPanel content = new JPanel(cl);

        // ===== DASHBOARD =====
        JPanel dash = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx=0; g.gridy=0;
        g.weightx=1; g.weighty=1;

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER,25,20));
        row.setOpaque(false);

        row.add(createCard("Total Students", ""+list.size(), new Color(76,175,80)));
        row.add(createCard("Classes", "TKPTG", new Color(33,150,243)));
        row.add(createCard("Status", "Active", new Color(156,39,176)));

        dash.add(row,g);

        // ===== STUDENT =====
        JPanel student = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2,4,10,10));
        txtName = new JTextField();
        txtClass = new JTextField();
        txtDob = new JSpinner(new SpinnerDateModel());
        txtDob.setEditor(new JSpinner.DateEditor(txtDob, "dd/MM/yyyy"));

        JButton add = new JButton("Add");
        JButton delete = new JButton("Delete");
        JButton edit = new JButton("Edit");

        form.add(new JLabel("Name"));
        form.add(new JLabel("Class"));
        form.add(new JLabel("DOB"));
        form.add(new JLabel(""));
        form.add(txtName);
        form.add(txtClass);
        form.add(txtDob);
        form.add(add);

        model = new DefaultTableModel(new String[]{"ID","Name","DOB","Class"},0){
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        table = new JTable(model);

        JPanel bottom = new JPanel();
        search = new JTextField(15);
        bottom.add(new JLabel("Search"));
        bottom.add(search);
        bottom.add(edit);
        bottom.add(delete);

        student.add(form,BorderLayout.NORTH);
        student.add(new JScrollPane(table),BorderLayout.CENTER);
        student.add(bottom,BorderLayout.SOUTH);

        content.add(dash,"HOME");
        content.add(student,"STUDENT");

        add(content,BorderLayout.CENTER);

        home.addActionListener(e->cl.show(content,"HOME"));
        manage.addActionListener(e->cl.show(content,"STUDENT"));

        // ===== ACTION =====
        add.addActionListener(e->{
            list.add(new Student(generateID(),
                    txtName.getText(),
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format((Date)txtDob.getValue()),
                    txtClass.getText()));
            save(); refresh();

            txtName.setText("");
            txtClass.setText("");
            txtDob.setValue(new Date());
        });

        delete.addActionListener(e->{
            int r=table.getSelectedRow();
            if(r>=0){ list.remove(r); save(); refresh(); }
        });

        edit.addActionListener(e->{
            int r = table.getSelectedRow();
            if(r >= 0){
                Student s = list.get(r);

                JDialog dialog = new JDialog(this, "Edit Student", true);
                dialog.setSize(350,250);
                dialog.setLocationRelativeTo(this);
                dialog.setLayout(new BorderLayout());

                JPanel panel = new JPanel(new GridLayout(3,2,10,10));
                panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

                JTextField nameField = new JTextField(s.name);
                JTextField classField = new JTextField(s.classId);
                JSpinner dobField = new JSpinner(new SpinnerDateModel());
                dobField.setEditor(new JSpinner.DateEditor(dobField, "dd/MM/yyyy"));

                try{
                    Date d = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(s.dob);
                    dobField.setValue(d);
                }catch(Exception ex){}

                panel.add(new JLabel("Name")); panel.add(nameField);
                panel.add(new JLabel("Class")); panel.add(classField);
                panel.add(new JLabel("DOB")); panel.add(dobField);

                JPanel actions = new JPanel();
                JButton saveBtn = new JButton("Save");
                JButton cancelBtn = new JButton("Cancel");

                actions.add(saveBtn);
                actions.add(cancelBtn);

                dialog.add(panel, BorderLayout.CENTER);
                dialog.add(actions, BorderLayout.SOUTH);

                saveBtn.addActionListener(ev->{
                    s.name = nameField.getText();
                    s.classId = classField.getText();
                    s.dob = new java.text.SimpleDateFormat("dd/MM/yyyy").format((Date)dobField.getValue());
                    save();
                    refresh();
                    dialog.dispose();
                });

                cancelBtn.addActionListener(ev-> dialog.dispose());

                dialog.setVisible(true);
            }
        });

        search.addKeyListener(new java.awt.event.KeyAdapter(){
            public void keyReleased(java.awt.event.KeyEvent e){
                model.setRowCount(0);
                for(Student s:list){
                    if(s.name.toLowerCase().contains(search.getText().toLowerCase())){
                        model.addRow(new Object[]{s.id,s.name,s.dob,s.classId});
                    }
                }
            }
        });

        refresh();
    }

    // ===== CARD =====
    JPanel createCard(String t,String v,Color color){
        JPanel p = new JPanel(new BorderLayout()){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),30,30);
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(200,110));
        p.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel title = new JLabel(t);
        title.setForeground(Color.WHITE);

        JLabel value = new JLabel(v);
        value.setForeground(Color.WHITE);
        value.setFont(new Font("Segoe UI",Font.BOLD,24));

        p.add(title,BorderLayout.NORTH);
        p.add(value,BorderLayout.CENTER);

        return p;
    }

    void refresh(){
        model.setRowCount(0);
        for(Student s:list){
            model.addRow(new Object[]{s.id,s.name,s.dob,s.classId});
        }
    }

    void save(){
        try{
            ObjectOutputStream o=new ObjectOutputStream(new FileOutputStream(FILE));
            o.writeObject(list);
            o.close();
        }catch(Exception e){}
    }

    void load(){
        try{
            ObjectInputStream o=new ObjectInputStream(new FileInputStream(FILE));
            list=(ArrayList<Student>)o.readObject();
            o.close();
        }catch(Exception e){ list=new ArrayList<>(); }
    }

    public static void main(String[] args){
        try{ UIManager.setLookAndFeel(new FlatLightLaf()); }catch(Exception e){}
        new LoginFrame().setVisible(true);
    }
}