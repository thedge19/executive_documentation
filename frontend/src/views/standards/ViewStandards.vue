<template>
    <main>
        <Navbar />

        <!-- Table-->
        <div class="container">
            <div class="row">
                <div class="col-md-12">
                    <h1 class="text-center">СП</h1>
                    <!--Add button -->
                    <a href="/addStandard" class="btn btn-primary">Добавить СП</a>
                    <table class="table table-striped">
                        <thead>
                          <tr>
                            <th scope="col">Id</th>
                            <th scope="col">Наименование</th>
                            <th scope="col">Действие</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="standard in standards" :key="standard.id">
                            <th scope="row">{{standard.id}}</th>
                            <td>{{standard.name}}</td>
                            <td>
                              <a class="btn btn-primary" :href="`/editStandard/${standard.id}`">Edit</a>
                              <button class="btn btn-danger mx-2" @click="deleteStandard(standard.id)">Delete</button>
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
        name: 'ViewStandards',
        components: {
            Navbar
        },
        data() {
            return {
                standards: []
            }
        },

        beforeMount(){
            this.getStandards()
        },

        methods: {
            getStandards(){
                fetch('http://localhost:8080/standards', {
                  mode: 'cors',
                  headers: {
                    'Content-Type': 'application/json',
                  }
                })
                .then(res => res.json())
                .then(data => {
                    this.standards = data
                    console.log(data)
                })
            },
            deleteStandard(id){
                fetch(`http://localhost:8080/standards/${id}`, {
                    method: 'DELETE'
                })
                .then(data => {
                    console.log(data)
                    this.getStandards()
                })
            }
        }
    }

</script>