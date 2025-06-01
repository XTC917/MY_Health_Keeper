import { createStore } from 'vuex'

export default createStore({
  state: {
    user: null,
    isAuthenticated: false
  },
  mutations: {
    SET_USER(state, user) {
      state.user = user
      state.isAuthenticated = !!user
    }
  },
  actions: {
    login({ commit }, userData) {
      // TODO: 实现登录逻辑
      commit('SET_USER', userData)
    },
    logout({ commit }) {
      commit('SET_USER', null)
    }
  },
  getters: {
    isAuthenticated: state => state.isAuthenticated,
    currentUser: state => state.user
  }
}) 