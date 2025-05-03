document.addEventListener('DOMContentLoaded', () => {
    const projectList = document.getElementById('project-list');
    const taskList = document.getElementById('task-list');
    const tasksHeader = document.getElementById('tasks-header');

    const addProjectBtn = document.getElementById('add-project-btn');
    const addTaskBtn = document.getElementById('add-task-btn');

    const projectFormContainer = document.getElementById('project-form-container');
    const projectForm = document.getElementById('project-form');
    const projectFormTitle = document.getElementById('project-form-title');
    const projectIdInput = document.getElementById('project-id');
    const projectNameInput = document.getElementById('project-name');
    const projectDescInput = document.getElementById('project-description');
    const projectStartInput = document.getElementById('project-start-date');
    const projectEndInput = document.getElementById('project-end-date');
    const cancelProjectBtn = document.getElementById('cancel-project-btn');

    const taskFormContainer = document.getElementById('task-form-container');
    const taskForm = document.getElementById('task-form');
    const taskFormTitle = document.getElementById('task-form-title');
    const taskIdInput = document.getElementById('task-id');
    const taskTitleInput = document.getElementById('task-title');
    const taskDescInput = document.getElementById('task-description');
    const taskStatusInput = document.getElementById('task-status');
    const taskDueDateInput = document.getElementById('task-due-date');
    const cancelTaskBtn = document.getElementById('cancel-task-btn');

    const messageArea = document.getElementById('message-area');

    const API_BASE_URL = 'http://localhost:8080/api';
    let currentProjectId = null;
    let projects = [];
    let tasks = [];


    function showMessage(message, isError = false) {
        messageArea.textContent = message;
        messageArea.className = isError ? 'error' : 'success';
        setTimeout(() => {
            messageArea.textContent = '';
            messageArea.className = '';
        }, 5000);
    }

    async function handleFetchError(response) {
        if (!response.ok) {
            let errorMsg = `Błąd HTTP: ${response.status}`;
            try {
                const errorData = await response.json();
                if (errorData.message) {
                    errorMsg += ` - ${errorData.message}`;
                } else if (errorData.messages) {
                    errorMsg += ` - Błędy walidacji: ${JSON.stringify(errorData.messages)}`;
                }
            } catch (e) {
            }
            throw new Error(errorMsg);
        }

        if (response.status === 204) {
            return null;
        }
        return await response.json();
    }

    async function fetchProjects() {
        try {
            const response = await fetch(`${API_BASE_URL}/projects`);
            projects = await handleFetchError(response);
            renderProjectList();
        } catch (error) {
            showMessage(`Nie udało się pobrać projektów: ${error.message}`, true);
            projectList.innerHTML = '<li class="error">Błąd ładowania projektów.</li>';
        }
    }

    async function fetchTasks(projectId) {
        try {
            const response = await fetch(`${API_BASE_URL}/projects/${projectId}/tasks`);
            tasks = await handleFetchError(response);
            renderTaskList(projectId);
        } catch (error) {
            showMessage(`Nie udało się pobrać zadań dla projektu ${projectId}: ${error.message}`, true);
            taskList.innerHTML = '<li class="error">Błąd ładowania zadań.</li>';
        }
    }

    async function saveProject(projectData) {
        const isEditing = !!projectData.id;
        const method = isEditing ? 'PUT' : 'POST';
        const url = isEditing ? `${API_BASE_URL}/projects/${projectData.id}` : `${API_BASE_URL}/projects`;

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(projectData),
            });
            const savedProject = await handleFetchError(response);
            showMessage(`Projekt "${savedProject.name}" ${isEditing ? 'zaktualizowany' : 'zapisany'} pomyślnie.`);
            hideProjectForm();
            fetchProjects(); // Odśwież listę projektów
        } catch (error) {
            showMessage(`Błąd zapisu projektu: ${error.message}`, true);
        }
    }


    async function deleteProject(projectId) {
        if (!confirm('Czy na pewno chcesz usunąć ten projekt i wszystkie jego zadania?')) {
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/projects/${projectId}`, {
                method: 'DELETE',
            });
            await handleFetchError(response);
            showMessage(`Projekt ${projectId} usunięty pomyślnie.`);

            if (currentProjectId === projectId) {
                currentProjectId = null;
                tasksHeader.textContent = 'Zadania (Wybierz projekt)';
                taskList.innerHTML = '<li>Wybierz projekt, aby zobaczyć zadania.</li>';
                addTaskBtn.disabled = true;
            }
            fetchProjects();
        } catch (error) {
            showMessage(`Błąd usuwania projektu: ${error.message}`, true);
        }
    }

    async function saveTask(taskData) {
        const isEditing = !!taskData.id;
        const method = isEditing ? 'PUT' : 'POST';
        const url = isEditing
            ? `${API_BASE_URL}/tasks/${taskData.id}`
            : `${API_BASE_URL}/projects/${currentProjectId}/tasks`;


        const dataToSend = {
            title: taskData.title,
            description: taskData.description,
            status: taskData.status,
            dueDate: taskData.dueDate || null
        };

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(dataToSend),
            });
            const savedTask = await handleFetchError(response);
            showMessage(`Zadanie "${savedTask.title}" ${isEditing ? 'zaktualizowane' : 'zapisane'} pomyślnie.`);
            hideTaskForm();
            fetchTasks(currentProjectId);
        } catch (error) {
            showMessage(`Błąd zapisu zadania: ${error.message}`, true);
        }
    }

    async function deleteTask(taskId) {
        if (!confirm('Czy na pewno chcesz usunąć to zadanie?')) {
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
                method: 'DELETE',
            });
            await handleFetchError(response);
            showMessage(`Zadanie ${taskId} usunięte pomyślnie.`);
            fetchTasks(currentProjectId);
        } catch (error) {
            showMessage(`Błąd usuwania zadania: ${error.message}`, true);
        }
    }

    function renderProjectList() {
        projectList.innerHTML = '';
        if (projects.length === 0) {
            projectList.innerHTML = '<li>Brak projektów. Dodaj nowy!</li>';
            return;
        }
        projects.forEach(project => {
            const li = document.createElement('li');
            li.textContent = project.name;
            li.dataset.projectId = project.id;

            if (project.id === currentProjectId) {
                li.classList.add('selected');
            }

            const actionsDiv = document.createElement('div');
            actionsDiv.classList.add('actions');

            const editBtn = document.createElement('button');
            editBtn.textContent = 'Edytuj';
            editBtn.onclick = (event) => {
                event.stopPropagation();
                showEditProjectForm(project);
            };

            const deleteBtn = document.createElement('button');
            deleteBtn.textContent = 'Usuń';
            deleteBtn.onclick = (event) => {
                event.stopPropagation();
                deleteProject(project.id);
            };

            actionsDiv.appendChild(editBtn);
            actionsDiv.appendChild(deleteBtn);
            li.appendChild(actionsDiv);


            li.addEventListener('click', () => {
                const currentlySelected = projectList.querySelector('.selected');
                if (currentlySelected) {
                    currentlySelected.classList.remove('selected');
                }
                li.classList.add('selected');

                currentProjectId = project.id;
                tasksHeader.textContent = `Zadania dla: ${project.name}`;
                addTaskBtn.disabled = false;
                fetchTasks(project.id);
                hideTaskForm();
            });

            projectList.appendChild(li);
        });
    }

    function renderTaskList(projectId) {
        taskList.innerHTML = '';
        if (tasks.length === 0) {
            taskList.innerHTML = `<li>Brak zadań dla tego projektu.</li>`;
            return;
        }
        tasks.forEach(task => {
            const li = document.createElement('li');

            let dueDateStr = task.dueDate
                ? new Date(task.dueDate).toLocaleString('pl-PL')
                : 'Brak';
            let statusStr = task.status.replace('_', ' ');


            const contentSpan = document.createElement('span');
            contentSpan.innerHTML = `
                <strong>${task.title}</strong><br>
                <small>Status: ${statusStr} | Termin: ${dueDateStr}</small>
            `;
            li.appendChild(contentSpan);


            const actionsDiv = document.createElement('div');
            actionsDiv.classList.add('actions');

            const editBtn = document.createElement('button');
            editBtn.textContent = 'Edytuj';
            editBtn.onclick = () => showEditTaskForm(task);


            const deleteBtn = document.createElement('button');
            deleteBtn.textContent = 'Usuń';
            deleteBtn.onclick = () => deleteTask(task.id);


            actionsDiv.appendChild(editBtn);
            actionsDiv.appendChild(deleteBtn);
            li.appendChild(actionsDiv);


            taskList.appendChild(li);
        });
    }



    function showAddProjectForm() {
        projectForm.reset();
        projectIdInput.value = '';
        projectFormTitle.textContent = 'Nowy Projekt';
        projectFormContainer.classList.remove('hidden');
        hideTaskForm();
    }

    function showEditProjectForm(project) {
        projectForm.reset();
        projectIdInput.value = project.id;
        projectNameInput.value = project.name;
        projectDescInput.value = project.description || '';
        // Format daty dla input type="date" to YYYY-MM-DD
        projectStartInput.value = project.startDate || '';
        projectEndInput.value = project.endDate || '';
        projectFormTitle.textContent = `Edytuj Projekt: ${project.name}`;
        projectFormContainer.classList.remove('hidden');
        hideTaskForm();
    }

    function hideProjectForm() {
        projectFormContainer.classList.add('hidden');
        projectForm.reset();
    }

    function showAddTaskForm() {
        taskForm.reset();
        taskIdInput.value = '';
        taskFormTitle.textContent = 'Nowe Zadanie';
        taskFormContainer.classList.remove('hidden');
        hideProjectForm();
    }

    function showEditTaskForm(task) {
        taskForm.reset();
        taskIdInput.value = task.id;
        taskTitleInput.value = task.title;
        taskDescInput.value = task.description || '';
        taskStatusInput.value = task.status;

        taskDueDateInput.value = task.dueDate ? task.dueDate.slice(0, 16) : '';
        taskFormTitle.textContent = `Edytuj Zadanie: ${task.title}`;
        taskFormContainer.classList.remove('hidden');
        hideProjectForm();
    }


    function hideTaskForm() {
        taskFormContainer.classList.add('hidden');
        taskForm.reset();
    }


    addProjectBtn.addEventListener('click', showAddProjectForm);


    cancelProjectBtn.addEventListener('click', hideProjectForm);


    projectForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const projectData = {
            id: projectIdInput.value || null,
            name: projectNameInput.value,
            description: projectDescInput.value,
            startDate: projectStartInput.value || null,
            endDate: projectEndInput.value || null,
        };
        saveProject(projectData);
    });

    addTaskBtn.addEventListener('click', showAddTaskForm);

    cancelTaskBtn.addEventListener('click', hideTaskForm);

    taskForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const taskData = {
            id: taskIdInput.value || null,
            title: taskTitleInput.value,
            description: taskDescInput.value,
            status: taskStatusInput.value,
            dueDate: taskDueDateInput.value ? taskDueDateInput.value + ':00' : null,
        };
        saveTask(taskData);
    });


    fetchProjects();

});