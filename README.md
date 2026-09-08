# 📊 StatMaster

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-1.7.2-4285F4?logo=android&logoColor=white)](https://developer.android.com/compose)
[![Supabase](https://img.shields.io/badge/Supabase-2.5.0-3ECF8E?logo=supabase&logoColor=white)](https://supabase.com/)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen?logo=android)](https://developer.android.com/studio/releases/platforms)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

</div>

<p align="center">
  <b>Образовательное Android-приложение для изучения теории вероятностей и математической статистики</b>
</p>

<p align="center">
  <b>Адаптивное тестирование на основе Item Response Theory (IRT)</b>
</p>

---

## 📖 О проекте

**StatMaster** — это современное образовательное приложение, которое помогает изучать теорию вероятностей и математическую статистику. Главная особенность — **адаптивная система тестирования**, которая подбирает вопросы под уровень знаний пользователя с использованием алгоритмов **Item Response Theory (IRT)**.

### 🎯 Основные возможности

| Возможность | Описание |
|-------------|----------|
| 🧠 **Адаптивное тестирование** | Алгоритм IRT подбирает вопросы под ваш уровень знаний |
| 👥 **Персонализированные рекомендации** | Коллаборативная фильтрация на основе похожих пользователей |
| 🔐 **Безопасная авторизация** | Email/пароль + Google Sign-In через Supabase Auth |
| 📚 **Образовательный контент** | Структурированные материалы с визуализацией |
| 📊 **Отслеживание прогресса** | Детальная статистика по темам и тестам |
| 📱 **Офлайн-режим** | Кэширование данных для работы без интернета |

---

## 🏗️ Архитектура

### Clean Architecture + MVVM

## 📱 Скриншоты

<div align="center">

| Главный экран | Авторизация | Список тем |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/ffb6af01-d4dd-414c-9e3e-7d3cec97270e" width="200"/> | <img src="https://github.com/user-attachments/assets/5c2ce59e-e4ee-4def-ba3b-d7f160c56fa0" width="200"/> | <img src="https://github.com/user-attachments/assets/97e7b54b-f2f6-41ce-98e3-aa47d8d4b806" width="200"/> |

| Базовый тест | Адаптивный тест | Статистика |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/c9e9e130-37d8-4003-ab00-0ef999c63f38" width="200"/> | <img src="https://github.com/user-attachments/assets/b3251745-d1a2-4ce0-b193-c54d512310a1" width="200"/> | <img src="https://github.com/user-attachments/assets/4726fd46-5982-4e72-84c1-d01c31809eaf" width="200"/> |

</div>

---

## 🧠 Адаптивный алгоритм

Приложение использует **Item Response Theory (IRT)** для оценки уровня знаний:

### Математическая модель

Вероятность правильного ответа рассчитывается по формуле:

```
P(θ) = 1 / (1 + e^(-D * (θ - b)))
```

где:
- **θ (тета)** — уровень способности пользователя
- **b** — сложность вопроса
- **D** — дискриминативность (обычно 1.7)

### Обновление уровня

После каждого ответа уровень способности обновляется:

```
θ_new = θ_old + learningRate * (correct - P(θ))
```

---

## 🚀 Быстрый старт

### 📋 Требования

- Android Studio Ladybug (2024.2.1) или новее
- JDK 17 или 21
- Android SDK (API 29+)
- Учетная запись Supabase

### 🔧 Установка

**1. Клонируйте репозиторий**
```bash
git clone https://github.com/gorgontt/StatMaster.git
cd StatMaster
```

**2. Настройте API ключи**
```bash
# Скопируйте пример конфигурации
cp local.properties.example local.properties

# Отредактируйте local.properties:
# SUPABASE_URL=https://your-project.supabase.co
# SUPABASE_KEY=your-anon-key
```

**3. Откройте проект в Android Studio**
```
File → Open → Выберите папку StatMaster
```

**4. Соберите и запустите**
```bash
./gradlew build
./gradlew installDebug
```

---

## 🧪 Тестирование

Проект покрыт unit-тестами для критической бизнес-логики.

### Запуск тестов
```bash
# Unit тесты
./gradlew test

# Инструментальные тесты
./gradlew connectedAndroidTest

# Отчет о покрытии
./gradlew testDebugUnitTestCoverage
```

---

## 📁 Структура проекта

```
StatMaster/
├── app/
│   ├── src/
│   │   ├── main/java/com/example/statmaster/
│   │   │   ├── adaptive/        # Адаптивный модуль (IRT, CF)
│   │   │   ├── auth/            # Авторизация (Email, Google)
│   │   │   ├── terver/          # Теория вероятностей
│   │   │   ├── stat/            # Статистика
│   │   │   ├── config/          # Конфигурация
│   │   │   └── ui/              # UI тема
│   │   ├── test/                # Unit тесты
│   │   └── androidTest/         # Инструментальные тесты
│   └── build.gradle.kts
├── docs/
│   └── screenshots/             # Скриншоты
├── README.md
├── LICENSE
└── .gitignore
```

---

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку (`git checkout -b feature/AmazingFeature`)
3. Закоммитьте изменения (`git commit -m 'Add feature'`)
4. Запушьте ветку (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

---

## 📄 Лицензия

Проект распространяется под лицензией **MIT**. Подробнее в файле [LICENSE](LICENSE).

---

## 📬 Контакты

<div align="center">

[![Telegram](https://img.shields.io/badge/Telegram-@gorgontt-blue?logo=telegram)](https://t.me/gorgontt)
[![Email](https://img.shields.io/badge/Email-gorgonttr@mail.ru-red?logo=gmail)](mailto:gorgonttr@mail.ru)

</div>

---

<p align="center">
  <i>⭐️ Если вам понравился проект, поставьте звезду на GitHub! ⭐️</i>
</p>
