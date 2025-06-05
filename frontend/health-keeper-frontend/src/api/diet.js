import api from './config'

export const DietService = {
  searchFood(query, category) {
    return api.get('/food/search', {
      params: { query, category }
    })
  },

  generateDietPlan(targetCalories, preferences) {
    return api.post('/diet/plan', {
      targetCalories,
      preferences
    })
  }
} 