# AWS SES Error Notifier

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A serverless solution for handling message processing failures by sending formatted email notifications via Amazon SES.

## Disclaimer 
This code is the result of some intense Vibe Coding, so the most part of the boilerplate code is AI generated.

## 📋 Overview

This AWS Lambda function automatically sends detailed error notifications via email when message processing fails in:
- SQS queues
- Kinesis streams
- DynamoDB streams
- Other event sources

Designed for reliability and security, it provides human-readable error reports with full message context while maintaining data integrity through HTML sanitization.

## 🏗 Architecture

### Components
```mermaid
graph TD
    A[Lambda Trigger] --> B[ConfigurationValidator]
    B --> C[EmailComposer]
    C --> D[SesMailer]
    D --> E[Amazon SES]



