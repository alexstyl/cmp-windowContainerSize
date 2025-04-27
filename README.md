# CMP Container Size

A Compose Multiplatform utility that provides an observable way of knowing the size of the window you are in.

## Installation

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.alexstyl:cmpcontainersize:1.0.0")
}
```

## Why do you need this

Compose Multiplatform has a built-in API for getting the size of the current window
(`LocalWindowInfo.current.containerSize`). However, this API is not current available on the Android target.

Until this, you can use this library.

## Basic Example

```kotlin
@Composable
fun App() {
    val containerSize = currentWindowContainerSize()

    if (containerSize.width >= 480.dp) {
        TabletLayout()
    } else {
        PhoneLayout()
    }
}
```

## About

Created by [Alex Styl](https://x.com/alexstyl). Tweet me with a cool thing you built.