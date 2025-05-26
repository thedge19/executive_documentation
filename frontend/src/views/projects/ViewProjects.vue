<template>
    <main>
        <Navbar />

        <!-- Table-->
        <div class="container">
            <div class="row">
                <div class="col-md-12">
                    <h1 class="text-center">Материалы</h1>
                    <!--Add button -->
                    <a href="/addProject" class="btn btn-primary">Добавить объект</a>
                    <table class="table table-striped">
                        <thead>
                          <tr>
                            <th scope="col">Id</th>
                            <th scope="col">Наименование</th>
                            <th scope="col">Действие</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="project in projects" :key="project.id">
                            <th scope="row">{{project.id}}</th>
                            <td>{{project.name}}</td>
                            <td>
                              <a class="btn btn-primary" :href="`/editProject/${project.id}`">Edit</a>
                              <button class="btn btn-danger mx-2" @click="deleteProject(project.id)">Delete</button>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                </div>
            </div>
        </div>

    </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue'

    export default {
        name: 'ViewProjects',
        components: {
            Navbar
        },
        data() {
            return {
                projects: []
            }
        },

        beforeMount(){
            this.getProjects()
        },

        methods: {
            getProjects(){
                fetch('http://localhost:8080/projects',
                    {
                      mode: 'cors',
                      headers: {
                        'Content-Type': 'application/json',
                      }
                    })
                .then(res => res.json())
                .then(data => {
                    this.projects = data
                    console.log(data)
                })
            },
            deleteProject(id){
                fetch(`http://localhost:8080/projects/${id}`, {
                    method: 'DELETE'
                })
                .then(data => {
                    console.log(data)
                    this.getProjects()
                })
            }
        }
    }

</script>