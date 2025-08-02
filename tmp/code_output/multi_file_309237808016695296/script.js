// script.js
function addTask() {
  const input = document.getElementById('taskInput');
  if (input.value.trim() === '') return;
  
  const li = document.createElement('li');
  li.textContent = input.value;
  li.onclick = () => li.classList.toggle('completed');
  
  document.getElementById('taskList').appendChild(li);
  input.value = '';
}