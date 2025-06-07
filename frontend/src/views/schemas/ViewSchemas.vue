<template>
    <main>
        <Navbar />

        <!-- Table-->
        <div class="container">
            <div class="row">
                <div class="col-md-12">
                    <h1 class="text-center">Исполнительные схемы</h1>
                    <!--Add button -->
                    <table class="table table-striped">
                        <thead>
                          <tr>
                            <th scope="col">Id</th>
                            <th scope="col">Номер схемы</th>
                            <th scope="col">Действие</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="schema in schemas" :key="schema.id">
                            <th scope="row">{{schema.id}}</th>
                            <td>{{schema.schemasActNumber}}</td>
                            <td>
                              <button class="btn btn-danger mx-2" @click="deleteSchema(schema.id)">Delete</button>
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
                schemas: []
            }
        },

        beforeMount(){
            this.getSchemas()
        },

        methods: {
            getSchemas(){
                fetch('http://localhost:8080/acts/schema',
                    {
                      mode: 'cors',
                      headers: {
                        'Content-Type': 'application/json',
                      }
                    })
                .then(res => res.json())
                .then(data => {
                    this.schemas = data
                    console.log(data)
                })
            },
            deleteSchema(id){
                fetch(`http://localhost:8080/acts/schema/${id}`, {
                    method: 'DELETE'
                })
                .then(data => {
                    console.log(data)
                    this.getSchemas()
                })
            }
        }
    }

</script>