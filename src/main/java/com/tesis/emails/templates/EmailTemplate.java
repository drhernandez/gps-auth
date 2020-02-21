package com.tesis.emails.templates;

import com.tesis.emails.models.EmailModel;

import java.util.function.Supplier;

public interface EmailTemplate extends Supplier<EmailModel> {
}
