import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://dumoaoivxujbxeanvjsw.supabase.co'
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR1bW9hb2l2eHVqYnhlYW52anN3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg5MzYyMDMsImV4cCI6MjA2NDUxMjIwM30.4qJF01gmYL8dNZ_43yRK9uWORZi0BpuIe50Pr39wdVw'

export const supabase = createClient(supabaseUrl, supabaseAnonKey)

export const uploadFile = async (file, bucket = 'public') => {
  try {
    const fileExt = file.name.split('.').pop()
    const fileName = `${Math.random().toString(36).substring(2)}.${fileExt}`
    const filePath = `${fileName}`

    const { error } = await supabase.storage
      .from(bucket)
      .upload(filePath, file)
      
    if (error) throw error

    const { data: { publicUrl } } = supabase.storage
      .from(bucket)
      .getPublicUrl(filePath)

    return publicUrl
  } catch (error) {
    console.error('Error uploading file:', error)
    throw error
  }
}

export const deleteFile = async (url, bucket = 'public') => {
  try {
    const path = url.split('/').pop()
    const { error } = await supabase.storage
      .from(bucket)
      .remove([path])

    if (error) throw error
  } catch (error) {
    console.error('Error deleting file:', error)
    throw error
  }
} 