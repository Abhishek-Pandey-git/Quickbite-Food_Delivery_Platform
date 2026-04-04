package com.app.quickbite.auth.entity;

/**
 * AUTH PROVIDER ENUM - To track how users registered/logged in
 * 
 * This enum helps us identify the source of authentication for each user.
 * It can be used for analytics, debugging, and implementing provider-specific logic if needed.
 * 
 * For example:
 * - If provider = NONE, we know the user registered with email/password.
 * - If provider = GOOGLE, we know the user logged in via Google OAuth2.0.
 */

public enum AuthProvider{
    NONE,
    GOOGLE
}