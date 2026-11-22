# JuDa Mass ⚖️📱

> **Calculus III (Matemáticas 3) — Final Project**  
> *Universidad Católica de Pereira (UCP)*  
> **Author:** JuDa Dev

---

## 📌 About The Project

**JuDa Mass** is an Android application developed as the final project for the **Calculus III (Multivariable Calculus)** course at Universidad Católica de Pereira.

### Why this application?

Traditional weight estimation methods rely either on physical scales or oversimplified body mass index (BMI) formulas that ignore individual geometry and body composition. 

The purpose of **JuDa Mass** is to demonstrate how **Triple Integrals** from multivariable calculus can be applied in the real world to estimate human mass and weight in real time through a smartphone camera without physical contact:

1. **Geometric Modeling via Triple Integrals:**
   - **Torso and Limbs:** Evaluated using triple integrals in **cylindrical coordinates**:
     $$\text{Volume} = \iiint r \, dz \, dr \, d\theta$$
   - **Cranial Region (Head):** Evaluated using triple integrals in **spherical ellipsoidal coordinates**:
     $$\text{Volume} = \iiint \rho^2 \sin(\phi) \, d\rho \, d\phi \, d\theta$$

2. **Heterogeneous Tissue Density Integration:**
   - Body mass is calculated by integrating the non-uniform tissue density function $\delta(r, \theta, z)$ across the total volume:
     $$\text{Mass} = \iiint_V \delta(r, \theta, z) \, dV$$
   - Modulated by clinical somatotypes (Ectomorph, Mesomorph, Endomorph).

3. **Computer Vision & Depth Sensing:**
   - Combines **Google ML Kit Pose Detection** (identifying anatomical joints and vertical bounds) with **Google ARCore Depth API** and defensive anthropometric fallback models to accurately estimate 3D body dimensions at 30–60 FPS.

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material Design 3 - Obsidian Titanium Dark Theme)
- **Architecture:** Clean Architecture + MVVM + Kotlin Coroutines & Flow
- **Vision & AR:** Google ML Kit Pose Detection + Google ARCore Depth API
- **Mathematics:** Pure Kotlin Multivariable Calculus Engine (Simpson 1/3 + Gauss-Legendre Quadrature)
- **Dependency Injection:** Koin

---

## 📜 License

This project is licensed under the MIT License — see the [LICENSE](./LICENSE) file for details.
