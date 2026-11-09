package com.orlo.app.data.datasource.impl.firestore

// Nombres de las colecciones y subcolecciones de Firestore. Son los mismos en las reglas de
// seguridad y en el modelo de datos que comparten la app y la web
const val USERS = "users"
const val RESULTS = "results"
const val LESSON_PROGRESS = "lessonProgress"
const val GARMENTS = "garments"
const val OUTFITS = "outfits"
const val CONVERSATIONS = "conversations"
const val MESSAGES = "messages"

// Un batch de Firestore admite hasta 500 operaciones
const val MAX_BATCH = 500
