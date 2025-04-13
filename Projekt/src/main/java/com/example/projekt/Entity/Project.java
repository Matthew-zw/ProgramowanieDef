package com.example.projekt.Entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(name = "start_Date")
    private LocalDate startDate;
    @Column(name = "end_Date")
    private LocalDate endDate;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();
    public void addTask(Task task){
        tasks.add(task);
        task.setProject(this);
    }
    public void removeTask(Task task){
        tasks.remove(task);
        task.setProject(null);
    }
}
