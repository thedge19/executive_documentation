<template>
    <main>
        <Navbar/>
        <div class="my-5">
            <div class="mx-auto w-25 " style="max-width:100%;">
                <h2 class="text-center mb-3">Обновить объект</h2>
                <form @submit.prevent="updateProject">
                    <!--name-->
                    <div class="row">
                        <div class="col-md-12 form-group mb-3">
                            <label for="name" class="form-label">Наименование</label>
                            <input id="name" type="text" name="name" class="form-control" placeholder="наименование"
                                   required v-model="project.name">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-12 form-group">
                            <input class="btn btn-primary w-100" type="submit" value="Обновить">
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </main>
</template>


<script>
import Navbar from '../../components/Navbar.vue';

export default {
    name: 'UpdateProject',
    components: {
        Navbar
    },

    data() {
        return {
            project: {
                id: '',
                name: '',
            }
        }
    },

    beforeMount() {
        this.getProject();
    },

    methods: {
        getProject() {
            fetch(`http://localhost:8080/projects/${this.$route.params.id}`)
                .then(res => res.json())
                .then(data => {
                    this.project = data;
                    console.log(this.project);
                })

        },
        updateProject() {
            fetch(`http://localhost:8080/projects/${this.$route.params.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(this.project)
            })
                .then(data => {
                    console.log(data);
                    this.$router.push('/projects');
                })
        }
    }
}

</script>