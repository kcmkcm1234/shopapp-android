package com.client.shop.ui.account

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.client.shop.R
import com.client.shop.ShopApplication
import com.client.shop.ext.getUpperCaseString
import com.client.shop.ui.account.contract.SignUpPresenter
import com.client.shop.ui.account.contract.SignUpView
import com.client.shop.ui.account.di.AuthModule
import com.client.shop.ui.custom.SimpleTextWatcher
import com.client.shop.ui.policy.PolicyActivity
import com.domain.entity.Policy
import com.ui.base.lce.BaseActivity
import kotlinx.android.synthetic.main.activity_sign_up.*
import javax.inject.Inject


class SignUpActivity : BaseActivity<Unit, SignUpView, SignUpPresenter>(),
        SignUpView {

    @Inject lateinit var signUpPresenter: SignUpPresenter
    private lateinit var emailTextWatcher: TextWatcher
    private lateinit var passwordTextWatcher: TextWatcher

    companion object {
        private const val PRIVACY_POLICY = "privacy_policy"
        private const val TERMS_OF_SERVICE = "terms_of_service"

        fun getStartIntent(context: Context, privacyPolicy: Policy?, termsOfService: Policy?): Intent {
            val intent = Intent(context, SignUpActivity::class.java)
            intent.putExtra(PRIVACY_POLICY, privacyPolicy)
            intent.putExtra(TERMS_OF_SERVICE, termsOfService)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(getString(R.string.sign_up))
        setupHints()
        setupInputListeners()
        setupInfoText()
        createButton.setOnClickListener {
            loadData(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        emailInput.removeTextChangedListener(emailTextWatcher)
        passwordInput.removeTextChangedListener(passwordTextWatcher)
    }

    //INIT

    override fun getContentView() = R.layout.activity_sign_up

    override fun createPresenter() = signUpPresenter

    override fun inject() {
        ShopApplication.appComponent.attachAuthComponent(AuthModule()).inject(this)
    }

    //SETUP

    private fun setupHints() {
        emailInputLayout.hint = getUpperCaseString(R.string.email)
        firstNameInputLayout.hint = getUpperCaseString(R.string.first_name)
        lastNameInputLayout.hint = getUpperCaseString(R.string.last_name)
        phoneInputLayout.hint = getUpperCaseString(R.string.phone_number)
        passwordInputLayout.hint = getUpperCaseString(R.string.create_password)
    }

    private fun setupInfoText() {

        val privacyPolicy: Policy? = intent.getParcelableExtra(PRIVACY_POLICY)
        val termsOfService: Policy? = intent.getParcelableExtra(TERMS_OF_SERVICE)

        if (privacyPolicy != null && termsOfService != null) {

            val privacyPolicySpan = SpannableString(getString(R.string.privacy_policy))
            val clickablePrivacyPolicySpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    startActivity(PolicyActivity.getStartIntent(this@SignUpActivity, privacyPolicy))
                }
            }
            privacyPolicySpan.setSpan(clickablePrivacyPolicySpan, 0, privacyPolicySpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val termsOfServiceSpan = SpannableString(getString(R.string.terms_of_service))
            val clickableTermsOfServiceSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    startActivity(PolicyActivity.getStartIntent(this@SignUpActivity, termsOfService))
                }
            }
            termsOfServiceSpan.setSpan(clickableTermsOfServiceSpan, 0, termsOfServiceSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val space = Character.SPACE_SEPARATOR.toChar().toString()
            policyText.text = TextUtils.concat(
                    getString(R.string.policy_text),
                    space,
                    privacyPolicySpan,
                    System.lineSeparator(),
                    getString(R.string.and),
                    space,
                    termsOfServiceSpan
            )
            policyText.movementMethod = LinkMovementMethod.getInstance()
            policyText.highlightColor = Color.TRANSPARENT
        }
    }

    private fun setupInputListeners() {
        emailTextWatcher = object : SimpleTextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkInputFields(emailInputLayout)
            }
        }
        passwordTextWatcher = object : SimpleTextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkInputFields(passwordInputLayout)
            }
        }
        emailInput.addTextChangedListener(emailTextWatcher)
        passwordInput.addTextChangedListener(passwordTextWatcher)
    }

    private fun checkInputFields(inputLayout: TextInputLayout) {
        createButton.isEnabled = emailInput.text.isNotBlank() && passwordInput.text.isNotBlank()
        if (inputLayout.isErrorEnabled) {
            inputLayout.isErrorEnabled = false
        }
    }

    //LCE

    override fun loadData(pullToRefresh: Boolean) {
        super.loadData(pullToRefresh)
        presenter.signUp(
                firstNameInput.text.trim().toString(),
                lastNameInput.text.trim().toString(),
                emailInput.text.trim().toString(),
                passwordInput.text.trim().toString(),
                phoneInput.text.trim().toString())
    }

    override fun showContent(data: Unit) {
        super.showContent(data)
        showMessage(R.string.register_success_message)
        finish()
    }

    override fun tryAgainButtonClicked() {
        loadData(true)
    }

    override fun showEmailError() {
        emailInputLayout.error = getString(R.string.invalid_email_error_message)
    }

    override fun showPasswordError() {
        passwordInputLayout.error = getString(R.string.invalid_password_error_message)
    }

    override fun onCheckPassed() {
        progressBar.show()
        createButton.visibility = View.INVISIBLE
    }

    override fun onFailure() {
        progressBar.hide()
        createButton.visibility = View.VISIBLE
    }
}