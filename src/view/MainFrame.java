/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import dao.*;
import model.*;
import utils.*;
import java.util.List;
import java.util.Date;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;

import java.text.SimpleDateFormat;      
import java.awt.event.ActionEvent;      
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.Calendar;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/**
 *
 * @author MyBook Z Series
 */
public class MainFrame extends javax.swing.JFrame {

    private TaskDAO taskDAO;
    private LecturerDAO lecturerDAO;
    private CourseDAO courseDAO;
    
    public MainFrame() {
        initComponents();

        // INIT DAO
        taskDAO = new TaskDAOImpl();
        lecturerDAO = new LecturerDAOImpl();
        courseDAO = new CourseDAOImpl();

        this.setLocationRelativeTo(null);

        if (Session.getUser() != null) {
            loadComboBoxes();
            loadTasksAsync();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Session user tidak ditemukan. Silakan login ulang.");
            dispose(); // tutup MainFrame
            return;
        }

        startDateTimeUpdater();

        SpinnerDateModel timeModel = new SpinnerDateModel();
        inputDeadlineJam.setModel(timeModel);
        JSpinner.DateEditor timeEditor =
                new JSpinner.DateEditor(inputDeadlineJam, "HH:mm");
        inputDeadlineJam.setEditor(timeEditor);
        inputDeadlineJam.setValue(new Date());
    }
    
    public void refreshCourseCombo() {
        cbCourse.removeAllItems();
        cbCourse.addItem("Pilih Mata Kuliah");

        List<Course> courses = courseDAO.getAll(Session.getUser().getId());
        for (Course c : courses) {
            cbCourse.addItem(c);
        }

        cbCourse.setSelectedIndex(0);
    }
    
    public void refreshLecturerCombo() {
        cbLecturer.removeAllItems();
        cbLecturer.addItem("Pilih Dosen");

        List<Lecturer> lecturers = lecturerDAO.getAll(Session.getUser().getId());
        for (Lecturer l : lecturers) {
            cbLecturer.addItem(l);
        }

        cbLecturer.setSelectedIndex(0);
    }

    
    private void loadComboBoxes() {
        int userId = Session.getUser().getId();

        cbLecturer.removeAllItems();
        cbLecturer.addItem("Pilih Dosen");
        for (Lecturer l : lecturerDAO.getAll(userId)) {
            cbLecturer.addItem(l);
        }

        cbCourse.removeAllItems();
        cbCourse.addItem("Pilih Mata Kuliah");
        for (Course c : courseDAO.getAll(userId)) {
            cbCourse.addItem(c);
        }

        cbLecturer.setSelectedIndex(0);
        cbCourse.setSelectedIndex(0);
    }

    private void refreshComboBoxes(int userId) {
        cbLecturer.removeAllItems();
        cbLecturer.addItem("Pilih Dosen");
        List<Lecturer> lecturers = lecturerDAO.getAll(userId);
        for (Lecturer l : lecturers) {
            cbLecturer.addItem(l);
        }

        cbCourse.removeAllItems();
        cbCourse.addItem("Pilih Mata Kuliah");
        List<Course> courses = courseDAO.getAll(userId);
        for (Course c : courses) {
            cbCourse.addItem(c);
        }

        cbLecturer.setSelectedIndex(0);
        cbCourse.setSelectedIndex(0);
    }
    
    private void loadTasksAsync() {
        new javax.swing.SwingWorker<List<Task>, Void>() {
            @Override
            protected List<Task> doInBackground() throws Exception {
                return taskDAO.getAllTasks();
            }

            @Override
            protected void done() {
                try {
                    List<Task> tasks = get();
                    DefaultTableModel model = new DefaultTableModel(
                            new Object[]{"ID", "Judul Tugas", "Deskripsi", "Deadline", "Status"}, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }

                        @Override
                        public Class<?> getColumnClass(int columnIndex) {
                            if (columnIndex == 3) return Date.class;
                            return Object.class;
                        }
                    };

                    for (Task t : tasks) {
                        model.addRow(new Object[]{
                                t.getId(),
                                t.getTitle(),
                                t.getDescription(),
                                t.getDeadline(),
                                t.isCompleted() ? "Selesai" : "Belum Selesai"
                        });
                    }

                    tableTasks.setModel(model);
                    TableStyler.styleTable(tableTasks);

                    tableTasks.getColumnModel().getColumn(0).setMinWidth(0);
                    tableTasks.getColumnModel().getColumn(0).setMaxWidth(0);

                    highlightOverdueTasks();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Gagal load tasks: " + e.getMessage());
                }
            }
        }.execute();
    }

    private int getSelectedLecturerId() {
        Object selected = cbLecturer.getSelectedItem();
        if (selected instanceof Lecturer) {
            return ((Lecturer) selected).getId();
        }
        return -1;
    }

    private int getSelectedCourseId() {
        Object selected = cbCourse.getSelectedItem();
        if (selected instanceof Course) {
            return ((Course) selected).getId();
        }
        return -1;
    }

    private Task getSelectedTask() {
        int selectedRow = tableTasks.getSelectedRow();
        if (selectedRow != -1) {
            int taskId = (int) tableTasks.getValueAt(selectedRow, 0);
            return taskDAO.getTaskById(taskId);
        }
        return null;
    }

    public void refreshCourseCombo(int userId) {
        cbCourse.removeAllItems();
        cbCourse.addItem("Pilih Mata Kuliah");

        List<Course> courses = courseDAO.getAll(userId);
        for (Course c : courses) {
            cbCourse.addItem(c);
        }

        cbCourse.setSelectedIndex(0);
    }

    public void refreshLecturerCombo(int userId) {
        cbLecturer.removeAllItems();
        cbLecturer.addItem("Pilih Dosen");

        List<Lecturer> lecturers = lecturerDAO.getAll(userId);
        for (Lecturer l : lecturers) {
            cbLecturer.addItem(l);
        }

        cbLecturer.setSelectedIndex(0);
    }


    private void highlightOverdueTasks() {

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {

                super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                // FORMAT DATE BIAR GA ADA ICT
                if (value instanceof Date) {
                    setText(sdf.format((Date) value));
                }

                try {
                    String status = table.getValueAt(row, 4).toString();
                    Date deadline = (Date) table.getValueAt(row, 3);
                    Date now = new Date();

                    if (!isSelected) {
                        if ("Belum Selesai".equals(status) && deadline.before(now)) {
                            setBackground(new java.awt.Color(255, 102, 102));
                            setForeground(java.awt.Color.WHITE);
                        } else {
                            setBackground(java.awt.Color.WHITE);
                            setForeground(java.awt.Color.BLACK);
                        }
                    }

                } catch (Exception e) {
                    setBackground(java.awt.Color.WHITE);
                    setForeground(java.awt.Color.BLACK);
                }

                return this;
            }
    };

    tableTasks.setDefaultRenderer(Object.class, renderer);
    tableTasks.setDefaultRenderer(Date.class, renderer);
}


    
    private void startDateTimeUpdater() {
    Timer timer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, HH:mm:ss, dd/MM/yyyy");;
            lblDateTime.setText(sdf.format(now));
        }
    });
    timer.start();
}
    
    private void exportLaporan() {
    String[] options = {"PDF", "Excel", "Batal"};
    int pilih = JOptionPane.showOptionDialog(
        this,
        "Pilih format laporan:",
        "Export Laporan",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        options,
        options[0]
    );

    if (pilih == 0) {
        ReportExporter.exportToPDF(tableTasks);
    } else if (pilih == 1) {
        ReportExporter.exportToExcel(tableTasks);
    }
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSidebar = new javax.swing.JPanel();
        btnManageCourse = new javax.swing.JButton();
        btnManageLecturer = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        btnHistory = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnLaporan = new javax.swing.JButton();
        panelMainContent = new javax.swing.JPanel();
        TaskPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        scrollTasks = new javax.swing.JScrollPane();
        tableTasks = new javax.swing.JTable();
        panelTaskToolbar = new javax.swing.JPanel();
        btnEditTask = new javax.swing.JButton();
        btnMarkTask = new javax.swing.JButton();
        btnDeleteTask = new javax.swing.JButton();
        btnRefreshTask = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtTitle = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtDescription = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        inputDeadlineTgl = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        inputDeadlineJam = new javax.swing.JSpinner();
        cbCourse = new javax.swing.JComboBox();
        cbLecturer = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblDateTime = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnAddTask = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplikasi Manjemen Tugas Gen-Z");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N

        panelSidebar.setBackground(new java.awt.Color(151, 175, 199));
        panelSidebar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        btnManageCourse.setText("Kelola Mata Kuliah");
        btnManageCourse.setBorderPainted(false);
        btnManageCourse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManageCourseActionPerformed(evt);
            }
        });

        btnManageLecturer.setText("Kelola Dosen");
        btnManageLecturer.setBorderPainted(false);
        btnManageLecturer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManageLecturerActionPerformed(evt);
            }
        });

        btnExit.setBackground(new java.awt.Color(227, 37, 37));
        btnExit.setForeground(new java.awt.Color(255, 255, 255));
        btnExit.setText("Logout");
        btnExit.setBorderPainted(false);
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        btnHistory.setText("History");
        btnHistory.setBorderPainted(false);
        btnHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHistoryActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Arial Black", 0, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Catat.In");

        btnLaporan.setForeground(new java.awt.Color(102, 102, 102));
        btnLaporan.setText("Laporan");
        btnLaporan.setBorderPainted(false);
        btnLaporan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLaporanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSidebarLayout = new javax.swing.GroupLayout(panelSidebar);
        panelSidebar.setLayout(panelSidebarLayout);
        panelSidebarLayout.setHorizontalGroup(
            panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSidebarLayout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(26, 26, 26))
            .addGroup(panelSidebarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnManageCourse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnManageLecturer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnHistory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnLaporan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelSidebarLayout.setVerticalGroup(
            panelSidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSidebarLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(btnManageCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnManageLecturer, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLaporan, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
        );

        panelMainContent.setBackground(new java.awt.Color(255, 255, 255));
        panelMainContent.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        TaskPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setText("Daftar Tugas");

        tableTasks.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tableTasks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollTasks.setViewportView(tableTasks);

        javax.swing.GroupLayout TaskPanelLayout = new javax.swing.GroupLayout(TaskPanel);
        TaskPanel.setLayout(TaskPanelLayout);
        TaskPanelLayout.setHorizontalGroup(
            TaskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TaskPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(TaskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollTasks)
                    .addGroup(TaskPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        TaskPanelLayout.setVerticalGroup(
            TaskPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TaskPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelTaskToolbar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnEditTask.setText("Edit");
        btnEditTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditTaskActionPerformed(evt);
            }
        });

        btnMarkTask.setText("Ubah Status Tugas");
        btnMarkTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarkTaskActionPerformed(evt);
            }
        });

        btnDeleteTask.setBackground(new java.awt.Color(227, 37, 37));
        btnDeleteTask.setForeground(new java.awt.Color(255, 255, 255));
        btnDeleteTask.setText("Hapus Tugas");
        btnDeleteTask.setBorderPainted(false);
        btnDeleteTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTaskActionPerformed(evt);
            }
        });

        btnRefreshTask.setText("Refresh");
        btnRefreshTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshTaskActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelTaskToolbarLayout = new javax.swing.GroupLayout(panelTaskToolbar);
        panelTaskToolbar.setLayout(panelTaskToolbarLayout);
        panelTaskToolbarLayout.setHorizontalGroup(
            panelTaskToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTaskToolbarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnMarkTask, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnRefreshTask, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEditTask, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 506, Short.MAX_VALUE)
                .addComponent(btnDeleteTask, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelTaskToolbarLayout.setVerticalGroup(
            panelTaskToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTaskToolbarLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelTaskToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditTask)
                    .addComponent(btnMarkTask)
                    .addComponent(btnDeleteTask)
                    .addComponent(btnRefreshTask))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Tambah Tugas Baru");

        jLabel4.setText("Judul Tugas");

        jLabel5.setText("Deskripsi Tugas");

        jLabel6.setText("Deadline Tugas");

        cbCourse.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cbLecturer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setText("Dosen Mata Kuliah");

        jLabel8.setText("Mata Kuliah");

        lblDateTime.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblDateTime.setText("[lblDateTime]");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(inputDeadlineJam, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(inputDeadlineTgl, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtTitle))
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDescription)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbCourse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel5)
                                            .addComponent(jLabel8))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(cbLecturer, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblDateTime)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblDateTime))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(jLabel8)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(inputDeadlineTgl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbCourse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbLecturer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inputDeadlineJam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        btnAddTask.setBackground(new java.awt.Color(0, 204, 102));
        btnAddTask.setForeground(new java.awt.Color(255, 255, 255));
        btnAddTask.setText("Tambah Tugas");
        btnAddTask.setBorderPainted(false);
        btnAddTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTaskActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddTask, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddTask)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelMainContentLayout = new javax.swing.GroupLayout(panelMainContent);
        panelMainContent.setLayout(panelMainContentLayout);
        panelMainContentLayout.setHorizontalGroup(
            panelMainContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainContentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelTaskToolbar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TaskPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelMainContentLayout.setVerticalGroup(
            panelMainContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainContentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TaskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTaskToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelMainContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelMainContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelSidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistoryActionPerformed
        HistoryDialog historyDialog = new HistoryDialog(this, true);
        historyDialog.setVisible(true);
    }//GEN-LAST:event_btnHistoryActionPerformed

    private void btnManageCourseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageCourseActionPerformed
        CourseDialog courseDialog = new CourseDialog(this, true); // 'this' berarti MainFrame sebagai parent
        courseDialog.setVisible(true); // tampilkan dialog
    }//GEN-LAST:event_btnManageCourseActionPerformed

    private void btnManageLecturerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageLecturerActionPerformed
        LecturerDialog lecturerDialog = new LecturerDialog(this, true);
        lecturerDialog.setVisible(true);
    }//GEN-LAST:event_btnManageLecturerActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            Session.setUser(null);

            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);

            this.dispose();
        }
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnAddTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTaskActionPerformed
        String title = txtTitle.getText().trim();
        String desc = txtDescription.getText().trim();

        Date date = inputDeadlineTgl.getDate();
        Date time = (Date) inputDeadlineJam.getValue();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int lecturerId = getSelectedLecturerId();
        int courseId = getSelectedCourseId();

        if (title.isEmpty() || desc.isEmpty() || date == null || lecturerId == -1 || courseId == -1) {
            JOptionPane.showMessageDialog(this, "Isi semua field terlebih dahulu!");
            return;
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(desc);
        task.setDeadline(cal.getTime());
        task.setCompleted(false);
        task.setLecturerId(lecturerId);
        task.setCourseId(courseId);

        // Simpan task
        if (taskDAO.addTask(task)) {
            JOptionPane.showMessageDialog(this, "Tugas berhasil ditambahkan!");
            loadTasksAsync();
            
            // RESET FORM
            txtTitle.setText("");
            txtDescription.setText("");

            inputDeadlineTgl.setDate(null);
            inputDeadlineJam.setValue(new Date());

            cbCourse.setSelectedIndex(0);
            cbCourse.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan tugas!");
        }
    }//GEN-LAST:event_btnAddTaskActionPerformed

    private void btnEditTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditTaskActionPerformed
        Task task = getSelectedTask();
        if (task != null) {
            EditDialog dialog = new EditDialog(this, true, task, taskDAO, lecturerDAO, courseDAO);
            dialog.setVisible(true);
            loadTasksAsync();
        } else {
            JOptionPane.showMessageDialog(this, "Pilih tugas dulu!");
        }
    }//GEN-LAST:event_btnEditTaskActionPerformed

    private void btnMarkTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMarkTaskActionPerformed
        Task task = getSelectedTask();
        if (task != null) {
            boolean newStatus = !task.isCompleted();
            if (taskDAO.updateTaskStatus(task.getId(), newStatus)) {
                JOptionPane.showMessageDialog(this, "Status tugas diupdate!");
                loadTasksAsync();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal update status!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih tugas dulu!");
        }
    }//GEN-LAST:event_btnMarkTaskActionPerformed

    private void btnRefreshTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshTaskActionPerformed
        loadTasksAsync();
    }//GEN-LAST:event_btnRefreshTaskActionPerformed

    private void btnDeleteTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteTaskActionPerformed
        Task task = getSelectedTask();
        if (task != null) {
            if (taskDAO.deleteTask(task.getId())) {
                JOptionPane.showMessageDialog(this, "Tugas dihapus!");
                loadTasksAsync();
            } else {
                JOptionPane.showMessageDialog(this, "Tugas gagal dihapus!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih tugas dulu!");
        }
    }//GEN-LAST:event_btnDeleteTaskActionPerformed

    private void btnLaporanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLaporanActionPerformed
        exportLaporan();
    }//GEN-LAST:event_btnLaporanActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel TaskPanel;
    private javax.swing.JButton btnAddTask;
    private javax.swing.JButton btnDeleteTask;
    private javax.swing.JButton btnEditTask;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnHistory;
    private javax.swing.JButton btnLaporan;
    private javax.swing.JButton btnManageCourse;
    private javax.swing.JButton btnManageLecturer;
    private javax.swing.JButton btnMarkTask;
    private javax.swing.JButton btnRefreshTask;
    private javax.swing.JComboBox cbCourse;
    private javax.swing.JComboBox cbLecturer;
    private javax.swing.JSpinner inputDeadlineJam;
    private com.toedter.calendar.JDateChooser inputDeadlineTgl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblDateTime;
    private javax.swing.JPanel panelMainContent;
    private javax.swing.JPanel panelSidebar;
    private javax.swing.JPanel panelTaskToolbar;
    private javax.swing.JScrollPane scrollTasks;
    private javax.swing.JTable tableTasks;
    private javax.swing.JTextField txtDescription;
    private javax.swing.JTextField txtTitle;
    // End of variables declaration//GEN-END:variables
}